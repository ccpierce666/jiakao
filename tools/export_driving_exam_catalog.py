import csv
import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "exam.db"
OUTPUT = ROOT / "driving_exam_v1_catalog.csv"


def main() -> None:
    connection = sqlite3.connect(f"file:{DATABASE.as_posix()}?mode=ro", uri=True)
    connection.row_factory = sqlite3.Row
    try:
        rows = connection.execute(
            """
            SELECT
                parent.id AS group_code,
                parent.name AS group_name,
                target.target_id AS bank_code,
                target.display_name AS bank_name,
                target.target_type,
                target.path
            FROM category_external_targets target
            JOIN category_nodes parent ON parent.id = target.category_id
            WHERE parent.path = '200000'
               OR parent.path LIKE '200000,%'
            ORDER BY
                CASE parent.id
                    WHEN '200001' THEN 1
                    WHEN '200002' THEN 2
                    WHEN '200003' THEN 3
                    WHEN '200005' THEN 4
                    WHEN '200006' THEN 5
                    WHEN '200007' THEN 6
                    ELSE 99
                END,
                CAST(target.target_id AS INTEGER)
            """
        ).fetchall()
    finally:
        connection.close()

    with OUTPUT.open("w", encoding="utf-8-sig", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(
            [
                "root_code",
                "root_name",
                "group_code",
                "group_name",
                "bank_code",
                "bank_name",
                "target_type",
                "path",
                "question_count",
                "data_status",
            ]
        )
        for row in rows:
            writer.writerow(
                [
                    200000,
                    "驾照考试",
                    row["group_code"],
                    row["group_name"],
                    row["bank_code"],
                    row["bank_name"],
                    row["target_type"],
                    row["path"],
                    0,
                    "当前数据库仅有目录，题目未采集",
                ]
            )

    print(f"rows: {len(rows)}")
    print(f"output: {OUTPUT}")


if __name__ == "__main__":
    main()
