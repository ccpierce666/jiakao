import sqlite3
import csv
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "exam.db"
OUTPUT = ROOT / "exam_category_mysql.sql"
MATCH_REPORT = ROOT / "category_sons_category_matches.csv"


def sql_text(value: object) -> str:
    if value is None or value == "":
        return "NULL"
    text = str(value).replace("\\", "\\\\").replace("'", "''")
    return f"'{text}'"


def sql_number(value: object, default: int = 0) -> str:
    if value is None or value == "":
        return str(default)
    return str(int(value))


def main() -> None:
    connection = sqlite3.connect(f"file:{DATABASE.as_posix()}?mode=ro", uri=True)
    connection.row_factory = sqlite3.Row
    try:
        rows = connection.execute(
            """
            SELECT
                id,
                parent_id,
                name,
                pinyin,
                path,
                sort,
                status,
                has_children,
                icon,
                description,
                title_display,
                banner,
                created_at,
                updated_at
            FROM category_nodes
            ORDER BY
                length(path) - length(replace(path, ',', '')),
                CAST(COALESCE(sort, 0) AS INTEGER),
                id
            """
        ).fetchall()
        inferred_rows = connection.execute(
            """
            SELECT
                node.id,
                node.parent_id,
                parent.name AS parent_name,
                node.name,
                node.path,
                node.has_children
            FROM category_nodes node
            LEFT JOIN category_nodes parent ON parent.id = node.parent_id
            WHERE node.source = 'category_sons_inferred'
            ORDER BY node.path
            """
        ).fetchall()
    finally:
        connection.close()

    lines = [
        "-- MySQL 8.0",
        "-- exam.db 四张分类表归并结果：438 个分类节点",
        "-- 来源：categories 401 个节点 + category_sons 补齐 37 个节点",
        "-- category_sons 的分类关系为 cid(父分类) -> target_id(子分类)，父级冲突数为 0",
        "-- status: 0=下架，1=上架",
        "-- 执行前可按实际应用修改变量。",
        "SET NAMES utf8mb4;",
        "SET @app_id = 1;",
        "SET @platform = 0;",
        "",
        "CREATE TABLE IF NOT EXISTS `exam_category` (",
        "  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,",
        "  `app_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '应用ID',",
        "  `platform` TINYINT NOT NULL DEFAULT 0 COMMENT '0=Android,1=iOS',",
        "  `code` VARCHAR(32) NOT NULL COMMENT '分类业务编码',",
        "  `parent_code` VARCHAR(32) NULL COMMENT '父分类业务编码，NULL表示根分类',",
        "  `name` VARCHAR(128) NOT NULL COMMENT '分类名称',",
        "  `pinyin` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '名称拼音',",
        "  `path` VARCHAR(512) NOT NULL COMMENT '从根到当前分类的编码路径',",
        "  `level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '分类层级，从1开始',",
        "  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '同级排序，越小越靠前',",
        "  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=下架，1=上架',",
        "  `has_children` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否存在子分类',",
        "  `icon` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '分类图标',",
        "  `description` TEXT NULL COMMENT '分类说明',",
        "  `title_display` JSON NULL COMMENT '标题展示配置',",
        "  `banner` JSON NULL COMMENT '分类Banner配置',",
        "  `source_created_at` DATETIME NULL COMMENT '源数据创建时间',",
        "  `source_updated_at` DATETIME NULL COMMENT '源数据更新时间',",
        "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,",
        "  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,",
        "  PRIMARY KEY (`id`),",
        "  UNIQUE KEY `uk_exam_category_scope_code` (`app_id`, `platform`, `code`),",
        "  KEY `idx_exam_category_parent` (`app_id`, `platform`, `parent_code`, `status`, `sort_order`),",
        "  KEY `idx_exam_category_status` (`app_id`, `platform`, `status`, `sort_order`),",
        "  CONSTRAINT `fk_exam_category_app` FOREIGN KEY (`app_id`) REFERENCES `app` (`id`),",
        "  CONSTRAINT `fk_exam_category_parent`",
        "    FOREIGN KEY (`app_id`, `platform`, `parent_code`)",
        "    REFERENCES `exam_category` (`app_id`, `platform`, `code`)",
        "    ON UPDATE CASCADE ON DELETE RESTRICT",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试分类';",
        "",
        "START TRANSACTION;",
        "",
    ]

    columns = (
        "`app_id`, `platform`, `code`, `parent_code`, `name`, `pinyin`, "
        "`path`, `level`, `sort_order`, `status`, `has_children`, `icon`, "
        "`description`, `title_display`, `banner`, `source_created_at`, "
        "`source_updated_at`"
    )

    batch_size = 100
    for start in range(0, len(rows), batch_size):
        batch = rows[start : start + batch_size]
        lines.append(f"INSERT INTO `exam_category` ({columns}) VALUES")
        tuples = []
        for row in batch:
            level = row["path"].count(",") + 1
            # 源库 status=0 表示有效；归一化为 gopdf 常用的 1=有效。
            normalized_status = 1
            tuples.append(
                "  ("
                + ", ".join(
                    [
                        "@app_id",
                        "@platform",
                        sql_text(row["id"]),
                        sql_text(row["parent_id"]),
                        sql_text(row["name"]),
                        sql_text(row["pinyin"]) if row["pinyin"] else "''",
                        sql_text(row["path"]),
                        str(level),
                        sql_number(row["sort"]),
                        str(normalized_status),
                        sql_number(row["has_children"]),
                        sql_text(row["icon"]) if row["icon"] else "''",
                        sql_text(row["description"]),
                        sql_text(row["title_display"]),
                        sql_text(row["banner"]),
                        sql_text(row["created_at"]),
                        sql_text(row["updated_at"]),
                    ]
                )
                + ")"
            )
        lines.append(",\n".join(tuples))
        lines.extend(
            [
                "ON DUPLICATE KEY UPDATE",
                "  `parent_code` = VALUES(`parent_code`),",
                "  `name` = VALUES(`name`),",
                "  `pinyin` = VALUES(`pinyin`),",
                "  `path` = VALUES(`path`),",
                "  `level` = VALUES(`level`),",
                "  `sort_order` = VALUES(`sort_order`),",
                "  `status` = VALUES(`status`),",
                "  `has_children` = VALUES(`has_children`),",
                "  `icon` = VALUES(`icon`),",
                "  `description` = VALUES(`description`),",
                "  `title_display` = VALUES(`title_display`),",
                "  `banner` = VALUES(`banner`),",
                "  `source_created_at` = VALUES(`source_created_at`),",
                "  `source_updated_at` = VALUES(`source_updated_at`),",
                "  `updated_at` = CURRENT_TIMESTAMP;",
                "",
            ]
        )

    lines.extend(
        [
            "COMMIT;",
            "",
            "-- 导入校验：应返回 total=438、roots=16、max_level=3。",
            "SELECT",
            "  COUNT(*) AS total,",
            "  SUM(`parent_code` IS NULL) AS roots,",
            "  MAX(`level`) AS max_level",
            "FROM `exam_category`",
            "WHERE `app_id` = @app_id AND `platform` = @platform;",
            "",
            "-- 断链校验：应返回 0。",
            "SELECT COUNT(*) AS orphan_count",
            "FROM `exam_category` child",
            "LEFT JOIN `exam_category` parent",
            "  ON parent.`app_id` = child.`app_id`",
            " AND parent.`platform` = child.`platform`",
            " AND parent.`code` = child.`parent_code`",
            "WHERE child.`app_id` = @app_id",
            "  AND child.`platform` = @platform",
            "  AND child.`parent_code` IS NOT NULL",
            "  AND parent.`id` IS NULL;",
            "",
        ]
    )

    OUTPUT.write_text("\n".join(lines), encoding="utf-8")
    with MATCH_REPORT.open("w", encoding="utf-8-sig", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(
            ["category_code", "category_name", "parent_code", "parent_name", "path", "has_children"]
        )
        for row in inferred_rows:
            writer.writerow(
                [
                    row["id"],
                    row["name"],
                    row["parent_id"],
                    row["parent_name"],
                    row["path"],
                    row["has_children"],
                ]
            )

    print(f"rows: {len(rows)}")
    print(f"inferred_rows: {len(inferred_rows)}")
    print(f"output: {OUTPUT}")
    print(f"match_report: {MATCH_REPORT}")


if __name__ == "__main__":
    main()
