import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "exam.db"
BACKUP = ROOT / "category_tables_backup_20260624.db"

SOURCE_TABLES = (
    "categories",
    "category_sons",
    "category_child_sons",
    "category_path",
)


def create_backup(source: sqlite3.Connection) -> None:
    if BACKUP.exists():
        BACKUP.unlink()

    backup = sqlite3.connect(BACKUP)
    try:
        for table in SOURCE_TABLES:
            create_sql = source.execute(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?",
                (table,),
            ).fetchone()[0]
            backup.execute(create_sql)

            columns = [
                row[1] for row in source.execute(f'PRAGMA table_info("{table}")')
            ]
            placeholders = ",".join("?" for _ in columns)
            column_list = ",".join(f'"{column}"' for column in columns)
            rows = source.execute(f'SELECT {column_list} FROM "{table}"')
            backup.executemany(
                f'INSERT INTO "{table}" ({column_list}) VALUES ({placeholders})',
                rows,
            )

        backup.commit()
    finally:
        backup.close()


def rebuild_normalized_tables(connection: sqlite3.Connection) -> None:
    connection.executescript(
        """
        DROP TABLE IF EXISTS category_external_targets;
        DROP TABLE IF EXISTS category_kaoshi;
        DROP TABLE IF EXISTS category_nodes;

        CREATE TABLE category_nodes (
            id            TEXT PRIMARY KEY,
            parent_id     TEXT,
            name          TEXT NOT NULL,
            pinyin        TEXT,
            path          TEXT,
            sort          INTEGER,
            status        INTEGER,
            has_children  INTEGER NOT NULL DEFAULT 0,
            icon          TEXT,
            description   TEXT,
            title_display TEXT,
            banner        TEXT,
            created_at    TEXT,
            updated_at    TEXT,
            source        TEXT NOT NULL,
            FOREIGN KEY (parent_id) REFERENCES category_nodes(id)
        );

        CREATE TABLE category_kaoshi (
            category_id     TEXT NOT NULL,
            kaoshi_id       TEXT NOT NULL,
            display_name    TEXT,
            sort            INTEGER,
            status          INTEGER,
            target_type     TEXT,
            item_type       TEXT,
            support_search  INTEGER,
            has_introduction INTEGER,
            path            TEXT,
            source_table    TEXT NOT NULL,
            source_row_id   TEXT,
            created_at      TEXT,
            updated_at      TEXT,
            PRIMARY KEY (category_id, kaoshi_id),
            FOREIGN KEY (category_id) REFERENCES category_nodes(id),
            FOREIGN KEY (kaoshi_id) REFERENCES kaoshi(id)
        );

        CREATE TABLE category_external_targets (
            category_id     TEXT NOT NULL,
            target_id       TEXT NOT NULL,
            target_type     TEXT,
            display_name    TEXT,
            sort            INTEGER,
            status          INTEGER,
            item_type       TEXT,
            support_search  INTEGER,
            path            TEXT,
            source_table    TEXT NOT NULL,
            source_row_id   TEXT,
            created_at      TEXT,
            updated_at      TEXT,
            PRIMARY KEY (category_id, target_id, source_table, source_row_id),
            FOREIGN KEY (category_id) REFERENCES category_nodes(id)
        );

        CREATE INDEX idx_category_nodes_parent
            ON category_nodes(parent_id, sort);
        CREATE INDEX idx_category_kaoshi_kaoshi
            ON category_kaoshi(kaoshi_id);
        CREATE INDEX idx_category_kaoshi_category_sort
            ON category_kaoshi(category_id, sort);
        CREATE INDEX idx_category_external_category_sort
            ON category_external_targets(category_id, sort);
        """
    )

    connection.execute(
        """
        INSERT INTO category_nodes (
            id, parent_id, name, pinyin, path, sort, status, has_children,
            icon, description, title_display, banner, created_at, updated_at,
            source
        )
        SELECT
            id,
            NULLIF(parentid, '0'),
            name,
            pinyin,
            path,
            CAST(NULLIF(sort, '') AS INTEGER),
            CAST(NULLIF(status, '') AS INTEGER),
            CASE haschildren WHEN '1' THEN 1 ELSE 0 END,
            icon,
            desc,
            title_display,
            banner,
            created_at,
            updated_at,
            COALESCE(source, 'categories')
        FROM categories
        """
    )

    connection.execute(
        """
        INSERT INTO category_nodes (
            id, parent_id, name, path, sort, status, has_children,
            created_at, updated_at, source
        )
        SELECT
            missing.cid,
            parent.cid,
            parent.name,
            parent.path,
            CAST(NULLIF(parent.sort, '') AS INTEGER),
            CAST(NULLIF(parent.status, '') AS INTEGER),
            1,
            parent.created_at,
            parent.updated_at,
            'category_sons_inferred'
        FROM (
            SELECT DISTINCT cid
            FROM category_sons
            WHERE NOT EXISTS (
                SELECT 1 FROM categories c WHERE c.id = category_sons.cid
            )
        ) missing
        JOIN category_sons parent
          ON parent.target_id = missing.cid
         AND parent.target_type = '0'
        """
    )

    connection.execute(
        """
        UPDATE category_nodes
        SET has_children = CASE
            WHEN EXISTS (
                SELECT 1
                FROM category_nodes child
                WHERE child.parent_id = category_nodes.id
            ) THEN 1
            ELSE 0
        END
        """
    )

    connection.execute(
        """
        WITH raw AS (
            SELECT
                'category_sons' AS source_table,
                id AS source_row_id,
                cid,
                target_id,
                target_type,
                name,
                sort,
                status,
                type,
                support_search,
                has_introduction,
                path,
                created_at,
                updated_at
            FROM category_sons
            UNION ALL
            SELECT
                'category_child_sons',
                id,
                cid,
                target_id,
                target_type,
                name,
                sort,
                status,
                type,
                support_search,
                has_introduction,
                path,
                created_at,
                updated_at
            FROM category_child_sons
        )
        INSERT INTO category_kaoshi (
            category_id, kaoshi_id, display_name, sort, status, target_type,
            item_type, support_search, has_introduction, path, source_table,
            source_row_id, created_at, updated_at
        )
        SELECT
            cid,
            target_id,
            name,
            CAST(NULLIF(sort, '') AS INTEGER),
            CAST(NULLIF(status, '') AS INTEGER),
            target_type,
            type,
            CAST(NULLIF(support_search, '') AS INTEGER),
            CAST(NULLIF(has_introduction, '') AS INTEGER),
            path,
            source_table,
            source_row_id,
            created_at,
            updated_at
        FROM raw
        WHERE NOT (
            target_type = '0'
            AND EXISTS (
                SELECT 1 FROM category_nodes n WHERE n.id = raw.target_id
            )
        )
          AND EXISTS (
              SELECT 1 FROM kaoshi k WHERE k.id = raw.target_id
          )
        """
    )

    connection.execute(
        """
        WITH raw AS (
            SELECT
                'category_sons' AS source_table,
                id AS source_row_id,
                cid,
                target_id,
                target_type,
                name,
                sort,
                status,
                type,
                support_search,
                path,
                created_at,
                updated_at
            FROM category_sons
            UNION ALL
            SELECT
                'category_child_sons',
                id,
                cid,
                target_id,
                target_type,
                name,
                sort,
                status,
                type,
                support_search,
                path,
                created_at,
                updated_at
            FROM category_child_sons
        )
        INSERT INTO category_external_targets (
            category_id, target_id, target_type, display_name, sort, status,
            item_type, support_search, path, source_table, source_row_id,
            created_at, updated_at
        )
        SELECT
            cid,
            target_id,
            target_type,
            name,
            CAST(NULLIF(sort, '') AS INTEGER),
            CAST(NULLIF(status, '') AS INTEGER),
            type,
            CAST(NULLIF(support_search, '') AS INTEGER),
            path,
            source_table,
            source_row_id,
            created_at,
            updated_at
        FROM raw
        WHERE NOT (
            target_type = '0'
            AND EXISTS (
                SELECT 1 FROM category_nodes n WHERE n.id = raw.target_id
            )
        )
          AND NOT EXISTS (
              SELECT 1 FROM kaoshi k WHERE k.id = raw.target_id
          )
        """
    )


def validate(connection: sqlite3.Connection) -> dict[str, int]:
    checks = {
        "category_nodes": "SELECT count(*) FROM category_nodes",
        "root_categories": (
            "SELECT count(*) FROM category_nodes WHERE parent_id IS NULL"
        ),
        "category_edges": (
            "SELECT count(*) FROM category_nodes WHERE parent_id IS NOT NULL"
        ),
        "category_kaoshi": "SELECT count(*) FROM category_kaoshi",
        "covered_kaoshi": (
            "SELECT count(DISTINCT kaoshi_id) FROM category_kaoshi"
        ),
        "all_kaoshi": "SELECT count(*) FROM kaoshi",
        "external_targets": (
            "SELECT count(*) FROM category_external_targets"
        ),
        "orphan_categories": (
            """
            SELECT count(*)
            FROM category_nodes child
            WHERE child.parent_id IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM category_nodes parent
                  WHERE parent.id = child.parent_id
              )
            """
        ),
        "orphan_category_kaoshi": (
            """
            SELECT count(*)
            FROM category_kaoshi relation
            WHERE NOT EXISTS (
                SELECT 1 FROM category_nodes c
                WHERE c.id = relation.category_id
            )
               OR NOT EXISTS (
                SELECT 1 FROM kaoshi k
                WHERE k.id = relation.kaoshi_id
            )
            """
        ),
    }
    return {
        name: connection.execute(sql).fetchone()[0]
        for name, sql in checks.items()
    }


def main() -> None:
    connection = sqlite3.connect(DATABASE)
    try:
        connection.execute("PRAGMA foreign_keys = OFF")
        create_backup(connection)
        with connection:
            rebuild_normalized_tables(connection)
        results = validate(connection)
    finally:
        connection.close()

    for name, value in results.items():
        print(f"{name}: {value}")
    print(f"backup: {BACKUP}")


if __name__ == "__main__":
    main()
