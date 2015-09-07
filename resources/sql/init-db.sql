-- name: init-posts!
-- Creates posts table if not exists.
CREATE TABLE IF NOT EXISTS posts
(id INTEGER PRIMARY KEY AUTOINCREMENT,
title TEXT,
date REAL,
content TEXT);

-- name: init-tags!
-- Creates tags table if not exists.
CREATE TABLE IF NOT EXISTS tags
(id INTEGER PRIMARY KEY,
post_id INTEGER,
tag TEXT);
 

