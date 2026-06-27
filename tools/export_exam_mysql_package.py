import argparse
import gzip
import json
import sqlite3
from pathlib import Path
from typing import Iterable, Sequence


ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "exam.db"
OUTPUT_DIR = ROOT / "mysql_exam_import"
APP_ID = 1
PLATFORM = 0
CHUNK_ROWS = 250_000
INSERT_BATCH_ROWS = 100


def sql_value(value: object) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    text = str(value)
    text = (
        text.replace("\\", "\\\\")
        .replace("\0", "\\0")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\x1a", "\\Z")
        .replace("'", "''")
    )
    return f"'{text}'"


def int_or_none(value: object) -> int | None:
    if value is None or value == "":
        return None
    return int(value)


def decimal_or_none(value: object) -> str | None:
    if value is None or value == "":
        return None
    return str(value)


def write_text(path: Path, content: str) -> None:
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(content, encoding="utf-8", newline="\n")
    temporary.replace(path)


def write_gzip_rows(
    path: Path,
    table: str,
    columns: Sequence[str],
    rows: Iterable[Sequence[object]],
) -> int:
    temporary = path.with_suffix(path.suffix + ".tmp")
    count = 0
    batch: list[str] = []
    column_sql = ", ".join(f"`{column}`" for column in columns)

    with gzip.open(
        temporary,
        mode="wt",
        encoding="utf-8",
        newline="\n",
        compresslevel=1,
    ) as output:
        output.write("SET NAMES utf8mb4;\n")
        output.write("SET FOREIGN_KEY_CHECKS=0;\n")
        output.write("START TRANSACTION;\n")

        for row in rows:
            batch.append("(" + ", ".join(sql_value(value) for value in row) + ")")
            count += 1
            if len(batch) >= INSERT_BATCH_ROWS:
                output.write(
                    f"INSERT INTO `{table}` ({column_sql}) VALUES\n"
                    + ",\n".join(batch)
                    + ";\n"
                )
                batch.clear()

        if batch:
            output.write(
                f"INSERT INTO `{table}` ({column_sql}) VALUES\n"
                + ",\n".join(batch)
                + ";\n"
            )

        output.write("COMMIT;\n")
        output.write("SET FOREIGN_KEY_CHECKS=1;\n")

    temporary.replace(path)
    return count


def schema_sql() -> str:
    return """-- MySQL 8.0 normalized exam database
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `exam_paper_question`;
DROP TABLE IF EXISTS `exam_kaoshi_paper`;
DROP TABLE IF EXISTS `exam_category_external_target`;
DROP TABLE IF EXISTS `exam_category_kaoshi`;
DROP TABLE IF EXISTS `exam_question`;
DROP TABLE IF EXISTS `exam_paper`;
DROP TABLE IF EXISTS `exam_kaoshi`;
DROP TABLE IF EXISTS `exam_category`;
DROP TABLE IF EXISTS `exam_data_meta`;

CREATE TABLE `exam_data_meta` (
  `meta_key` VARCHAR(128) NOT NULL,
  `meta_value` TEXT NULL,
  PRIMARY KEY (`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试数据集元信息';

CREATE TABLE `exam_category` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `code` BIGINT UNSIGNED NOT NULL,
  `parent_code` BIGINT UNSIGNED NULL,
  `name` VARCHAR(128) NOT NULL,
  `pinyin` VARCHAR(128) NOT NULL DEFAULT '',
  `path` VARCHAR(512) NOT NULL,
  `level` TINYINT UNSIGNED NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=下架,1=上架',
  `has_children` TINYINT(1) NOT NULL DEFAULT 0,
  `icon` VARCHAR(512) NOT NULL DEFAULT '',
  `description` TEXT NULL,
  `title_display` JSON NULL,
  `banner` JSON NULL,
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`, `platform`, `code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试分类树';

CREATE TABLE `exam_kaoshi` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `info` TEXT NULL,
  `type` SMALLINT NULL,
  `with_news` TINYINT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `price` DECIMAL(12,2) NULL,
  `origin_price` DECIMAL(12,2) NULL,
  `collection_number` BIGINT NOT NULL DEFAULT 0,
  `buy_number` BIGINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=下架,1=上架',
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  `legacy_category` VARCHAR(64) NULL,
  `kaoshi_type` SMALLINT NULL,
  `is_new` TINYINT NOT NULL DEFAULT 0,
  `target_type` SMALLINT NULL,
  `extra_json` JSON NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`, `platform`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试或题库产品';

CREATE TABLE `exam_category_kaoshi` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `category_code` BIGINT UNSIGNED NOT NULL,
  `kaoshi_id` BIGINT UNSIGNED NOT NULL,
  `display_name` VARCHAR(128) NOT NULL DEFAULT '',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `target_type` SMALLINT NULL,
  `item_type` SMALLINT NULL,
  `support_search` TINYINT NULL,
  `has_introduction` TINYINT NULL,
  `source_table` VARCHAR(32) NOT NULL,
  `source_row_id` VARCHAR(64) NULL,
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  PRIMARY KEY (`app_id`, `platform`, `category_code`, `kaoshi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类与考试多对多关系';

CREATE TABLE `exam_category_external_target` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `category_code` BIGINT UNSIGNED NOT NULL,
  `target_id` VARCHAR(64) NOT NULL,
  `target_type` SMALLINT NULL,
  `display_name` VARCHAR(128) NOT NULL DEFAULT '',
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `item_type` SMALLINT NULL,
  `support_search` TINYINT NULL,
  `path` VARCHAR(512) NULL,
  `source_table` VARCHAR(32) NOT NULL,
  `source_row_id` VARCHAR(64) NOT NULL,
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  PRIMARY KEY (`app_id`, `platform`, `category_code`, `target_id`, `source_table`, `source_row_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类中的非考试入口';

CREATE TABLE `exam_paper` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `id` BIGINT UNSIGNED NOT NULL,
  `uid` BIGINT NULL,
  `name` VARCHAR(255) NOT NULL,
  `legacy_pcategory` VARCHAR(64) NULL,
  `tag` VARCHAR(255) NULL,
  `legacy_new_category` VARCHAR(64) NULL,
  `legacy_final_category` VARCHAR(64) NULL,
  `legacy_pdirectory` VARCHAR(64) NULL,
  `legacy_path` VARCHAR(512) NULL,
  `paper_type` SMALLINT NULL,
  `difficulty` SMALLINT NULL,
  `paper_level` VARCHAR(64) NULL,
  `learn_count` BIGINT NOT NULL DEFAULT 0,
  `question_total` INT NOT NULL DEFAULT 0,
  `question_updated_at` DATETIME NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `support_search` TINYINT NULL,
  `forbid_search` TINYINT NULL,
  `password` VARCHAR(255) NULL,
  `price` DECIMAL(12,2) NULL,
  `open_exchange` TINYINT NULL,
  `enable_download` TINYINT NULL,
  `service_time` INT NULL,
  `intro` TEXT NULL,
  `exercise_number` BIGINT NOT NULL DEFAULT 0,
  `collection_number` BIGINT NOT NULL DEFAULT 0,
  `recommend` BIGINT NULL COMMENT '源数据中包含推荐记录ID/时间戳，不是布尔值',
  `buy_number` BIGINT NOT NULL DEFAULT 0,
  `watermark` TINYINT NULL,
  `admin_uid` BIGINT NULL,
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  `update_info` LONGTEXT NULL,
  `sub_id` VARCHAR(64) NULL,
  `auto_analysed` TINYINT NULL,
  `source` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`, `platform`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='唯一试卷';

CREATE TABLE `exam_kaoshi_paper` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `kaoshi_id` BIGINT UNSIGNED NOT NULL,
  `paper_id` BIGINT UNSIGNED NOT NULL,
  `kaoshi_name_snapshot` VARCHAR(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`app_id`, `platform`, `kaoshi_id`, `paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试与试卷多对多关系';

CREATE TABLE `exam_question` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `id` BIGINT UNSIGNED NOT NULL,
  `uid` BIGINT NULL,
  `chapter` VARCHAR(255) NULL,
  `difficulty` SMALLINT NULL,
  `difficulty_score` DECIMAL(10,4) NULL,
  `parent_id` BIGINT NULL,
  `legacy_path` VARCHAR(512) NULL,
  `paper_type` SMALLINT NULL,
  `question_text` LONGTEXT NOT NULL,
  `question_type` SMALLINT NULL,
  `type_name` VARCHAR(128) NULL,
  `question_number` VARCHAR(64) NULL,
  `options` LONGTEXT NULL,
  `answer` LONGTEXT NULL,
  `analysis` LONGTEXT NULL,
  `content_hash` VARCHAR(255) NULL,
  `question_context_id` VARCHAR(64) NULL,
  `question_images` LONGTEXT NULL,
  `answer_images` LONGTEXT NULL,
  `extra` LONGTEXT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `is_show` TINYINT NULL,
  `editor` VARCHAR(128) NULL,
  `source_created_at` DATETIME NULL,
  `source_updated_at` DATETIME NULL,
  `note` LONGTEXT NULL,
  `note_is_public` TINYINT NULL,
  `note_id` VARCHAR(64) NULL,
  `self_analysis` LONGTEXT NULL,
  `ai_analysis` LONGTEXT NULL,
  `ai_analysis_like` BIGINT NULL,
  `point` LONGTEXT NULL,
  `chapters` LONGTEXT NULL,
  `all_right` BIGINT NULL,
  `all_wrong` BIGINT NULL,
  `all_accuracy` DECIMAL(10,4) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`, `platform`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

CREATE TABLE `exam_paper_question` (
  `app_id` BIGINT UNSIGNED NOT NULL,
  `platform` TINYINT NOT NULL DEFAULT 0,
  `paper_id` BIGINT UNSIGNED NOT NULL,
  `question_id` BIGINT UNSIGNED NOT NULL,
  `sequence` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`app_id`, `platform`, `paper_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷题目及顺序';

SET FOREIGN_KEY_CHECKS=1;
"""


def constraints_sql() -> str:
    return """-- Run after all data files have been imported.
SET NAMES utf8mb4;

ALTER TABLE `exam_category`
  ADD KEY `idx_exam_category_parent` (`app_id`, `platform`, `parent_code`, `status`, `sort_order`),
  ADD KEY `idx_exam_category_status` (`app_id`, `platform`, `status`, `sort_order`),
  ADD CONSTRAINT `fk_exam_category_app`
    FOREIGN KEY (`app_id`) REFERENCES `app` (`id`),
  ADD CONSTRAINT `fk_exam_category_parent`
    FOREIGN KEY (`app_id`, `platform`, `parent_code`)
    REFERENCES `exam_category` (`app_id`, `platform`, `code`)
    ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE `exam_category_kaoshi`
  ADD KEY `idx_exam_category_kaoshi_kaoshi` (`app_id`, `platform`, `kaoshi_id`),
  ADD KEY `idx_exam_category_kaoshi_sort` (`app_id`, `platform`, `category_code`, `status`, `sort_order`),
  ADD CONSTRAINT `fk_exam_category_kaoshi_category`
    FOREIGN KEY (`app_id`, `platform`, `category_code`)
    REFERENCES `exam_category` (`app_id`, `platform`, `code`),
  ADD CONSTRAINT `fk_exam_category_kaoshi_kaoshi`
    FOREIGN KEY (`app_id`, `platform`, `kaoshi_id`)
    REFERENCES `exam_kaoshi` (`app_id`, `platform`, `id`);

ALTER TABLE `exam_category_external_target`
  ADD KEY `idx_exam_category_external_sort` (`app_id`, `platform`, `category_code`, `status`, `sort_order`),
  ADD CONSTRAINT `fk_exam_category_external_category`
    FOREIGN KEY (`app_id`, `platform`, `category_code`)
    REFERENCES `exam_category` (`app_id`, `platform`, `code`);

ALTER TABLE `exam_kaoshi`
  ADD KEY `idx_exam_kaoshi_status` (`app_id`, `platform`, `status`, `sort_order`),
  ADD KEY `idx_exam_kaoshi_name` (`app_id`, `platform`, `name`),
  ADD CONSTRAINT `fk_exam_kaoshi_app`
    FOREIGN KEY (`app_id`) REFERENCES `app` (`id`);

ALTER TABLE `exam_paper`
  ADD KEY `idx_exam_paper_status` (`app_id`, `platform`, `status`),
  ADD KEY `idx_exam_paper_name` (`app_id`, `platform`, `name`),
  ADD CONSTRAINT `fk_exam_paper_app`
    FOREIGN KEY (`app_id`) REFERENCES `app` (`id`);

ALTER TABLE `exam_kaoshi_paper`
  ADD KEY `idx_exam_kaoshi_paper_paper` (`app_id`, `platform`, `paper_id`),
  ADD CONSTRAINT `fk_exam_kaoshi_paper_kaoshi`
    FOREIGN KEY (`app_id`, `platform`, `kaoshi_id`)
    REFERENCES `exam_kaoshi` (`app_id`, `platform`, `id`),
  ADD CONSTRAINT `fk_exam_kaoshi_paper_paper`
    FOREIGN KEY (`app_id`, `platform`, `paper_id`)
    REFERENCES `exam_paper` (`app_id`, `platform`, `id`);

ALTER TABLE `exam_question`
  ADD KEY `idx_exam_question_type` (`app_id`, `platform`, `question_type`, `status`),
  ADD KEY `idx_exam_question_parent` (`app_id`, `platform`, `parent_id`),
  ADD CONSTRAINT `fk_exam_question_app`
    FOREIGN KEY (`app_id`) REFERENCES `app` (`id`);

ALTER TABLE `exam_paper_question`
  ADD KEY `idx_exam_paper_question_order` (`app_id`, `platform`, `paper_id`, `sequence`),
  ADD KEY `idx_exam_paper_question_question` (`app_id`, `platform`, `question_id`),
  ADD CONSTRAINT `fk_exam_paper_question_paper`
    FOREIGN KEY (`app_id`, `platform`, `paper_id`)
    REFERENCES `exam_paper` (`app_id`, `platform`, `id`),
  ADD CONSTRAINT `fk_exam_paper_question_question`
    FOREIGN KEY (`app_id`, `platform`, `question_id`)
    REFERENCES `exam_question` (`app_id`, `platform`, `id`);

ANALYZE TABLE
  `exam_category`,
  `exam_category_kaoshi`,
  `exam_kaoshi`,
  `exam_paper`,
  `exam_kaoshi_paper`,
  `exam_question`,
  `exam_paper_question`;
"""


def verification_sql() -> str:
    return """SET NAMES utf8mb4;
SELECT 'exam_category' table_name, COUNT(*) row_count FROM exam_category
UNION ALL SELECT 'exam_category_kaoshi', COUNT(*) FROM exam_category_kaoshi
UNION ALL SELECT 'exam_category_external_target', COUNT(*) FROM exam_category_external_target
UNION ALL SELECT 'exam_kaoshi', COUNT(*) FROM exam_kaoshi
UNION ALL SELECT 'exam_paper', COUNT(*) FROM exam_paper
UNION ALL SELECT 'exam_kaoshi_paper', COUNT(*) FROM exam_kaoshi_paper
UNION ALL SELECT 'exam_question', COUNT(*) FROM exam_question
UNION ALL SELECT 'exam_paper_question', COUNT(*) FROM exam_paper_question
UNION ALL SELECT 'exam_data_meta', COUNT(*) FROM exam_data_meta;

SELECT COUNT(*) AS orphan_category
FROM exam_category child
LEFT JOIN exam_category parent
  ON parent.app_id=child.app_id AND parent.platform=child.platform
 AND parent.code=child.parent_code
WHERE child.parent_code IS NOT NULL AND parent.code IS NULL;

SELECT COUNT(*) AS orphan_category_kaoshi
FROM exam_category_kaoshi relation
LEFT JOIN exam_category category
  ON category.app_id=relation.app_id AND category.platform=relation.platform
 AND category.code=relation.category_code
LEFT JOIN exam_kaoshi kaoshi
  ON kaoshi.app_id=relation.app_id AND kaoshi.platform=relation.platform
 AND kaoshi.id=relation.kaoshi_id
WHERE category.code IS NULL OR kaoshi.id IS NULL;

SELECT COUNT(*) AS orphan_kaoshi_paper
FROM exam_kaoshi_paper relation
LEFT JOIN exam_kaoshi kaoshi
  ON kaoshi.app_id=relation.app_id AND kaoshi.platform=relation.platform
 AND kaoshi.id=relation.kaoshi_id
LEFT JOIN exam_paper paper
  ON paper.app_id=relation.app_id AND paper.platform=relation.platform
 AND paper.id=relation.paper_id
WHERE kaoshi.id IS NULL OR paper.id IS NULL;

SELECT COUNT(*) AS orphan_paper_question
FROM exam_paper_question relation
LEFT JOIN exam_paper paper
  ON paper.app_id=relation.app_id AND paper.platform=relation.platform
 AND paper.id=relation.paper_id
LEFT JOIN exam_question question
  ON question.app_id=relation.app_id AND question.platform=relation.platform
 AND question.id=relation.question_id
WHERE paper.id IS NULL OR question.id IS NULL;
"""


def query_examples_sql() -> str:
    return """-- MySQL 8 query examples
SET NAMES utf8mb4;
SET @app_id = 1;
SET @platform = 0;
SET @category_code = 100334;

-- 1. 查询当前分类及全部下级分类。
WITH RECURSIVE category_tree AS (
  SELECT `code`, `parent_code`, `name`, `level`
  FROM `exam_category`
  WHERE `app_id`=@app_id
    AND `platform`=@platform
    AND `code`=@category_code
    AND `status`=1

  UNION ALL

  SELECT child.`code`, child.`parent_code`, child.`name`, child.`level`
  FROM `exam_category` child
  JOIN category_tree parent ON child.`parent_code`=parent.`code`
  WHERE child.`app_id`=@app_id
    AND child.`platform`=@platform
    AND child.`status`=1
)
SELECT * FROM category_tree ORDER BY `level`, `code`;

-- 2. 通过当前分类及全部下级分类查询考试。
WITH RECURSIVE category_tree AS (
  SELECT `code`
  FROM `exam_category`
  WHERE `app_id`=@app_id AND `platform`=@platform
    AND `code`=@category_code AND `status`=1
  UNION ALL
  SELECT child.`code`
  FROM `exam_category` child
  JOIN category_tree parent ON child.`parent_code`=parent.`code`
  WHERE child.`app_id`=@app_id
    AND child.`platform`=@platform
    AND child.`status`=1
)
SELECT DISTINCT
  category.`code` AS category_code,
  category.`name` AS category_name,
  kaoshi.`id` AS kaoshi_id,
  kaoshi.`name` AS kaoshi_name
FROM category_tree tree
JOIN `exam_category` category
  ON category.`app_id`=@app_id
 AND category.`platform`=@platform
 AND category.`code`=tree.`code`
JOIN `exam_category_kaoshi` relation
  ON relation.`app_id`=category.`app_id`
 AND relation.`platform`=category.`platform`
 AND relation.`category_code`=category.`code`
 AND relation.`status`=1
JOIN `exam_kaoshi` kaoshi
  ON kaoshi.`app_id`=relation.`app_id`
 AND kaoshi.`platform`=relation.`platform`
 AND kaoshi.`id`=relation.`kaoshi_id`
 AND kaoshi.`status`=1
ORDER BY category.`code`, relation.`sort_order`, kaoshi.`id`;

-- 3. 从分类一直查询到题目。生产环境请务必分页。
WITH RECURSIVE category_tree AS (
  SELECT `code`
  FROM `exam_category`
  WHERE `app_id`=@app_id AND `platform`=@platform
    AND `code`=@category_code AND `status`=1
  UNION ALL
  SELECT child.`code`
  FROM `exam_category` child
  JOIN category_tree parent ON child.`parent_code`=parent.`code`
  WHERE child.`app_id`=@app_id
    AND child.`platform`=@platform
    AND child.`status`=1
)
SELECT DISTINCT
  category.`code` AS category_code,
  category.`name` AS category_name,
  kaoshi.`id` AS kaoshi_id,
  kaoshi.`name` AS kaoshi_name,
  paper.`id` AS paper_id,
  paper.`name` AS paper_name,
  relation_question.`sequence`,
  question.`id` AS question_id,
  question.`question_text`,
  question.`options`,
  question.`answer`,
  question.`analysis`
FROM category_tree tree
JOIN `exam_category` category
  ON category.`app_id`=@app_id
 AND category.`platform`=@platform
 AND category.`code`=tree.`code`
JOIN `exam_category_kaoshi` relation_category
  ON relation_category.`app_id`=category.`app_id`
 AND relation_category.`platform`=category.`platform`
 AND relation_category.`category_code`=category.`code`
 AND relation_category.`status`=1
JOIN `exam_kaoshi` kaoshi
  ON kaoshi.`app_id`=relation_category.`app_id`
 AND kaoshi.`platform`=relation_category.`platform`
 AND kaoshi.`id`=relation_category.`kaoshi_id`
 AND kaoshi.`status`=1
JOIN `exam_kaoshi_paper` relation_paper
  ON relation_paper.`app_id`=kaoshi.`app_id`
 AND relation_paper.`platform`=kaoshi.`platform`
 AND relation_paper.`kaoshi_id`=kaoshi.`id`
JOIN `exam_paper` paper
  ON paper.`app_id`=relation_paper.`app_id`
 AND paper.`platform`=relation_paper.`platform`
 AND paper.`id`=relation_paper.`paper_id`
 AND paper.`status`=1
JOIN `exam_paper_question` relation_question
  ON relation_question.`app_id`=paper.`app_id`
 AND relation_question.`platform`=paper.`platform`
 AND relation_question.`paper_id`=paper.`id`
JOIN `exam_question` question
  ON question.`app_id`=relation_question.`app_id`
 AND question.`platform`=relation_question.`platform`
 AND question.`id`=relation_question.`question_id`
 AND question.`status`=1
ORDER BY kaoshi.`id`, paper.`id`, relation_question.`sequence`
LIMIT 100 OFFSET 0;
"""


def importer_python() -> str:
    return r'''import argparse
import gzip
import os
import subprocess
from pathlib import Path


def run_mysql(mysql, common_args, data):
    process = subprocess.Popen([mysql, *common_args], stdin=subprocess.PIPE)
    try:
        for chunk in iter(lambda: data.read(1024 * 1024), b""):
            process.stdin.write(chunk)
    finally:
        process.stdin.close()
    code = process.wait()
    if code:
        raise SystemExit(code)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mysql", default="mysql")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", default="3306")
    parser.add_argument("--user", required=True)
    parser.add_argument("--database", required=True)
    args = parser.parse_args()

    root = Path(__file__).resolve().parent
    common = [
        "--default-character-set=utf8mb4",
        "-h", args.host,
        "-P", args.port,
        "-u", args.user,
        args.database,
    ]
    print("Password is read from MYSQL_PWD.")

    ordered = [root / "00_schema.sql"]
    ordered.extend(sorted(root.glob("[0-8][0-9]_*.sql.gz")))
    ordered.append(root / "99_constraints.sql")
    ordered.append(root / "99_verify.sql")

    for path in ordered:
        print(f"importing {path.name}")
        if path.suffix == ".gz":
            with gzip.open(path, "rb") as stream:
                run_mysql(args.mysql, common, stream)
        else:
            with path.open("rb") as stream:
                run_mysql(args.mysql, common, stream)


if __name__ == "__main__":
    main()
'''


def readme_text(manifest: dict[str, object]) -> str:
    return f"""# MySQL 8 导入包

关系：

`exam_category -> exam_category_kaoshi -> exam_kaoshi -> exam_kaoshi_paper -> exam_paper -> exam_paper_question -> exam_question`

数据量：

- 分类：{manifest['exam_category']}
- 分类考试关系：{manifest['exam_category_kaoshi']}
- 分类外部入口：{manifest['exam_category_external_target']}
- 考试：{manifest['exam_kaoshi']}
- 唯一试卷：{manifest['exam_paper']}
- 考试试卷关系：{manifest['exam_kaoshi_paper']}
- 题目：{manifest['exam_question']}
- 试卷题目关系：{manifest['exam_paper_question']}

旧表归并：

- `categories/category_sons/category_child_sons/category_path` → `exam_category`
- 分类中指向考试的记录 → `exam_category_kaoshi`
- 分类中非考试入口 → `exam_category_external_target`
- `papers` 中重复的 `paper_id` → `exam_paper` 唯一记录
- 考试和试卷挂载关系 → `exam_kaoshi_paper`
- `papers.question_ids/questions.paperid` → `exam_paper_question`
- 空表 `paper_qtype_count` 不迁移，可按题型聚合生成

## 导入

1. 确保目标库已有 `app.id={APP_ID}`。
2. 将 MySQL 密码放入环境变量 `MYSQL_PWD`。
3. 执行：

```powershell
$env:MYSQL_PWD='你的密码'
python .\\import_mysql.py --user root --database gopdf
```

如 mysql.exe 不在 PATH：

```powershell
python .\\import_mysql.py --mysql C:\\path\\to\\mysql.exe --user root --database gopdf
```

导入器会依次执行建表、压缩数据文件、索引与外键、最终校验。

常用查询参见 `QUERY_EXAMPLES.sql`。
"""


def export_small_tables(connection: sqlite3.Connection, manifest: dict[str, int]) -> None:
    manifest["exam_data_meta"] = write_gzip_rows(
        OUTPUT_DIR / "01_exam_data_meta.sql.gz",
        "exam_data_meta",
        ["meta_key", "meta_value"],
        connection.execute("SELECT key, value FROM meta ORDER BY key"),
    )

    category_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["id"]),
            int(row["parent_id"]) if row["parent_id"] else None,
            row["name"],
            row["pinyin"] or "",
            row["path"],
            row["path"].count(",") + 1,
            int_or_none(row["sort"]) or 0,
            1,
            int_or_none(row["has_children"]) or 0,
            row["icon"] or "",
            row["description"],
            row["title_display"],
            row["banner"],
            row["created_at"],
            row["updated_at"],
        )
        for row in connection.execute(
            """
            SELECT * FROM category_nodes
            ORDER BY
                length(path) - length(replace(path, ',', '')),
                COALESCE(sort, 0),
                id
            """
        )
    )
    manifest["exam_category"] = write_gzip_rows(
        OUTPUT_DIR / "02_exam_category.sql.gz",
        "exam_category",
        [
            "app_id", "platform", "code", "parent_code", "name", "pinyin",
            "path", "level", "sort_order", "status", "has_children", "icon",
            "description", "title_display", "banner", "source_created_at",
            "source_updated_at",
        ],
        category_rows,
    )

    category_kaoshi_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["category_id"]),
            int(row["kaoshi_id"]),
            row["display_name"] or "",
            int_or_none(row["sort"]) or 0,
            1,
            int_or_none(row["target_type"]),
            int_or_none(row["item_type"]),
            int_or_none(row["support_search"]),
            int_or_none(row["has_introduction"]),
            row["source_table"],
            row["source_row_id"],
            row["created_at"],
            row["updated_at"],
        )
        for row in connection.execute(
            "SELECT * FROM category_kaoshi ORDER BY category_id, sort, kaoshi_id"
        )
    )
    manifest["exam_category_kaoshi"] = write_gzip_rows(
        OUTPUT_DIR / "03_exam_category_kaoshi.sql.gz",
        "exam_category_kaoshi",
        [
            "app_id", "platform", "category_code", "kaoshi_id",
            "display_name", "sort_order", "status", "target_type",
            "item_type", "support_search", "has_introduction", "source_table",
            "source_row_id", "source_created_at", "source_updated_at",
        ],
        category_kaoshi_rows,
    )

    external_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["category_id"]),
            row["target_id"],
            int_or_none(row["target_type"]),
            row["display_name"] or "",
            int_or_none(row["sort"]) or 0,
            1,
            int_or_none(row["item_type"]),
            int_or_none(row["support_search"]),
            row["path"],
            row["source_table"],
            row["source_row_id"],
            row["created_at"],
            row["updated_at"],
        )
        for row in connection.execute(
            """
            SELECT * FROM category_external_targets
            ORDER BY category_id, sort, target_id
            """
        )
    )
    manifest["exam_category_external_target"] = write_gzip_rows(
        OUTPUT_DIR / "04_exam_category_external_target.sql.gz",
        "exam_category_external_target",
        [
            "app_id", "platform", "category_code", "target_id", "target_type",
            "display_name", "sort_order", "status", "item_type",
            "support_search", "path", "source_table", "source_row_id",
            "source_created_at", "source_updated_at",
        ],
        external_rows,
    )

    kaoshi_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["id"]),
            row["name"],
            row["info"],
            int_or_none(row["type"]),
            int_or_none(row["with_news"]),
            int_or_none(row["sort"]) or 0,
            decimal_or_none(row["price"]),
            decimal_or_none(row["origin_price"]),
            int_or_none(row["collection_number"]) or 0,
            int_or_none(row["buy_number"]) or 0,
            1,
            row["created_at"],
            row["updated_at"],
            row["category"],
            int_or_none(row["kaoshi_type"]),
            int_or_none(row["new_kaoshi"]) or 0,
            int_or_none(row["target_type"]),
            row["extra_json"],
        )
        for row in connection.execute("SELECT * FROM kaoshi ORDER BY CAST(id AS INTEGER)")
    )
    manifest["exam_kaoshi"] = write_gzip_rows(
        OUTPUT_DIR / "05_exam_kaoshi.sql.gz",
        "exam_kaoshi",
        [
            "app_id", "platform", "id", "name", "info", "type", "with_news",
            "sort_order", "price", "origin_price", "collection_number",
            "buy_number", "status", "source_created_at", "source_updated_at",
            "legacy_category", "kaoshi_type", "is_new", "target_type",
            "extra_json",
        ],
        kaoshi_rows,
    )

    paper_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["paper_id"]),
            int_or_none(row["uid"]),
            row["name"] or "",
            row["pcategory"],
            row["tag"],
            row["new_category"],
            row["final_category"],
            row["pdirectory"],
            row["path"],
            int_or_none(row["ptype"]),
            int_or_none(row["difficulty"]),
            row["plevel"],
            int_or_none(row["learn_count"]) or 0,
            int_or_none(row["question_total"]) or 0,
            row["question_updated_at"],
            1,
            int_or_none(row["support_search"]),
            int_or_none(row["forbid_search"]),
            row["password"],
            decimal_or_none(row["price"]),
            int_or_none(row["open_exchange"]),
            int_or_none(row["enable_download"]),
            int_or_none(row["service_time"]),
            row["intro"],
            int_or_none(row["exercise_number"]) or 0,
            int_or_none(row["collection_number"]) or 0,
            int_or_none(row["recommend"]),
            int_or_none(row["buy_number"]) or 0,
            int_or_none(row["watermark"]),
            int_or_none(row["admin_uid"]),
            row["created_at"],
            row["updated_at"],
            row["update_info"],
            row["sub_id"],
            int_or_none(row["auto_analysed"]),
            row["source"],
        )
        for row in connection.execute(
            """
            WITH ranked AS (
                SELECT *,
                       ROW_NUMBER() OVER (
                           PARTITION BY paper_id
                           ORDER BY COALESCE(updated_at, '') DESC, id DESC
                       ) AS row_rank
                FROM papers
            )
            SELECT * FROM ranked
            WHERE row_rank = 1
            ORDER BY CAST(paper_id AS INTEGER)
            """
        )
    )
    manifest["exam_paper"] = write_gzip_rows(
        OUTPUT_DIR / "06_exam_paper.sql.gz",
        "exam_paper",
        [
            "app_id", "platform", "id", "uid", "name", "legacy_pcategory",
            "tag", "legacy_new_category", "legacy_final_category",
            "legacy_pdirectory", "legacy_path", "paper_type", "difficulty",
            "paper_level", "learn_count", "question_total",
            "question_updated_at", "status", "support_search", "forbid_search",
            "password", "price", "open_exchange", "enable_download",
            "service_time", "intro", "exercise_number", "collection_number",
            "recommend", "buy_number", "watermark", "admin_uid",
            "source_created_at", "source_updated_at", "update_info", "sub_id",
            "auto_analysed", "source",
        ],
        paper_rows,
    )

    kaoshi_paper_rows = (
        (
            APP_ID,
            PLATFORM,
            int(row["kaoshi_id"]),
            int(row["paper_id"]),
            row["kaoshi_name"] or "",
        )
        for row in connection.execute(
            """
            SELECT kaoshi_id, paper_id, kaoshi_name
            FROM papers
            ORDER BY CAST(kaoshi_id AS INTEGER), CAST(paper_id AS INTEGER)
            """
        )
    )
    manifest["exam_kaoshi_paper"] = write_gzip_rows(
        OUTPUT_DIR / "07_exam_kaoshi_paper.sql.gz",
        "exam_kaoshi_paper",
        ["app_id", "platform", "kaoshi_id", "paper_id", "kaoshi_name_snapshot"],
        kaoshi_paper_rows,
    )


def question_row(row: sqlite3.Row) -> tuple[object, ...]:
    return (
        APP_ID,
        PLATFORM,
        int(row["id"]),
        int_or_none(row["uid"]),
        row["chapter"],
        int_or_none(row["difficulty"]),
        decimal_or_none(row["difficulty_score"]),
        int_or_none(row["parentid"]),
        row["path"],
        int_or_none(row["ptype"]),
        row["question"] or "",
        int_or_none(row["qtype"]),
        row["type_name"],
        row["number"],
        row["options"],
        row["answer"],
        row["analysis"],
        row["hash"],
        row["question_context_id"],
        row["question_images"],
        row["answer_images"],
        row["extra"],
        1,
        int_or_none(row["is_show"]),
        row["editor"],
        row["created_at"],
        row["updated_at"],
        row["note"],
        int_or_none(row["note_is_public"]),
        row["note_id"],
        row["self_analysis"],
        row["ai_analysis"],
        int_or_none(row["ai_analysis_like"]),
        row["point"],
        row["chapters"],
        int_or_none(row["all_right"]),
        int_or_none(row["all_wrong"]),
        decimal_or_none(row["all_accuracy"]),
    )


def export_chunked(
    connection: sqlite3.Connection,
    query: str,
    filename_prefix: str,
    table: str,
    columns: Sequence[str],
    transform,
) -> int:
    cursor = connection.execute(query)
    total = 0
    chunk_number = 0
    while True:
        source_rows = cursor.fetchmany(CHUNK_ROWS)
        if not source_rows:
            break
        chunk_number += 1
        path = OUTPUT_DIR / f"{filename_prefix}_{chunk_number:04d}.sql.gz"
        count = write_gzip_rows(
            path,
            table,
            columns,
            (transform(row) for row in source_rows),
        )
        total += count
        print(f"{path.name}: {count} rows")
    return total


def export_large_tables(connection: sqlite3.Connection, manifest: dict[str, int]) -> None:
    manifest["exam_question"] = export_chunked(
        connection,
        "SELECT * FROM questions ORDER BY CAST(id AS INTEGER)",
        "08_exam_question",
        "exam_question",
        [
            "app_id", "platform", "id", "uid", "chapter", "difficulty",
            "difficulty_score", "parent_id", "legacy_path", "paper_type",
            "question_text", "question_type", "type_name", "question_number",
            "options", "answer", "analysis", "content_hash",
            "question_context_id", "question_images", "answer_images", "extra",
            "status", "is_show", "editor", "source_created_at",
            "source_updated_at", "note", "note_is_public", "note_id",
            "self_analysis", "ai_analysis", "ai_analysis_like", "point",
            "chapters", "all_right", "all_wrong", "all_accuracy",
        ],
        question_row,
    )

    manifest["exam_paper_question"] = export_chunked(
        connection,
        """
        SELECT paper_id, question_id, seq
        FROM paper_question_ids
        ORDER BY paper_id, seq
        """,
        "09_exam_paper_question",
        "exam_paper_question",
        ["app_id", "platform", "paper_id", "question_id", "sequence"],
        lambda row: (
            APP_ID,
            PLATFORM,
            int(row["paper_id"]),
            int(row["question_id"]),
            int(row["seq"]),
        ),
    )


def validate_source(connection: sqlite3.Connection) -> None:
    expected = {
        "category_nodes": 438,
        "category_kaoshi": 2129,
        "category_external_targets": 121,
        "kaoshi": 2124,
        "unique_papers": 4239,
        "kaoshi_papers": 4475,
        "questions": 7181574,
        "paper_questions": 7181574,
    }
    actual = {
        "category_nodes": connection.execute(
            "SELECT count(*) FROM category_nodes"
        ).fetchone()[0],
        "category_kaoshi": connection.execute(
            "SELECT count(*) FROM category_kaoshi"
        ).fetchone()[0],
        "category_external_targets": connection.execute(
            "SELECT count(*) FROM category_external_targets"
        ).fetchone()[0],
        "kaoshi": connection.execute("SELECT count(*) FROM kaoshi").fetchone()[0],
        "unique_papers": connection.execute(
            "SELECT count(DISTINCT paper_id) FROM papers"
        ).fetchone()[0],
        "kaoshi_papers": connection.execute(
            "SELECT count(*) FROM papers"
        ).fetchone()[0],
        "questions": connection.execute(
            "SELECT count(*) FROM questions"
        ).fetchone()[0],
        "paper_questions": connection.execute(
            "SELECT count(*) FROM paper_question_ids"
        ).fetchone()[0],
    }
    if actual != expected:
        raise RuntimeError(f"source counts changed: expected={expected}, actual={actual}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--mode",
        choices=("all", "small", "large"),
        default="all",
    )
    args = parser.parse_args()

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    write_text(OUTPUT_DIR / "00_schema.sql", schema_sql())
    write_text(OUTPUT_DIR / "99_constraints.sql", constraints_sql())
    write_text(OUTPUT_DIR / "99_verify.sql", verification_sql())
    write_text(OUTPUT_DIR / "QUERY_EXAMPLES.sql", query_examples_sql())
    write_text(OUTPUT_DIR / "import_mysql.py", importer_python())

    connection = sqlite3.connect(f"file:{DATABASE.as_posix()}?mode=ro", uri=True)
    connection.row_factory = sqlite3.Row
    manifest_path = OUTPUT_DIR / "manifest.json"
    manifest: dict[str, int] = {}
    if manifest_path.exists():
        manifest.update(json.loads(manifest_path.read_text(encoding="utf-8")))

    try:
        validate_source(connection)
        if args.mode in ("all", "small"):
            export_small_tables(connection, manifest)
            write_text(
                manifest_path,
                json.dumps(manifest, ensure_ascii=False, indent=2),
            )
        if args.mode in ("all", "large"):
            export_large_tables(connection, manifest)
            write_text(
                manifest_path,
                json.dumps(manifest, ensure_ascii=False, indent=2),
            )
    finally:
        connection.close()

    if {
        "exam_category",
        "exam_category_kaoshi",
        "exam_category_external_target",
        "exam_kaoshi",
        "exam_paper",
        "exam_kaoshi_paper",
        "exam_question",
        "exam_paper_question",
    }.issubset(manifest):
        write_text(OUTPUT_DIR / "README.md", readme_text(manifest))

    print(json.dumps(manifest, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
