-- name: save-post<!
-- Creates new post
INSERT INTO posts(title,date,content)
VALUES (:title,:date,:content);

-- name: all-posts
-- Get all posts available
SELECT * FROM posts;

-- name: posts-range
-- Get :x posts from :offset
SELECT * FROM posts
ORDER BY id DESC
LIMIT :n OFFSET :offset; 

-- name: count-posts
-- Get the number of posts available.
SELECT COUNT(*) AS n_posts FROM posts;

-- name: id->post
-- Search the post by id.
SELECT * FROM posts WHERE id = :id;

-- name: id->post&tags
-- Search the post and associated tags.
SELECT posts.id, posts.title, posts.content, posts.date, tags.tag
FROM posts, tags
WHERE posts.id = :id
AND tags.post_id = :id

-- name: date-between
-- Search posts posted between start and end (in epoc)
SELECT id,title,date FROM posts
WHERE date BETWEEN :start AND :end
ORDER BY date DESC;

--name: search-text
-- Search posts where its title or content is LIKE :text
SELECT id,title,date,content FROM posts
WHERE title LIKE :text
OR content LIKE :text;

-- name: new-post!
-- Create new post.
INSERT INTO posts (title,date,content)
VALUES (:title,:date,:content);

-- name: delete-post!
-- Delete post by id.
DELETE FROM posts
WHERE id = :id;

-- name: edit-post!
-- Edit post specified by id, to new value :title, :date, content
UPDATE posts 
SET title = :title, date = :date, content = :content
WHERE id = :id
