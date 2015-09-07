-- name: post-id->tags
-- Given post-id, return tags belong to.
SELECT tags.id,tags.tag
FROM tags
WHERE tags.post_id = :id

-- name: search-by-tag
-- Given tag name, return posts the tag belongs to.
SELECT posts.id, posts.title, posts.content, posts.date
FROM posts, tags
WHERE posts.id = tags.post_id
AND tags.tag LIKE :q

-- name: all-tags
-- Get all tags available.
SELECT DISTINCT tag FROM tags; 

-- name: assoc-tag!
-- Associate tag-id with post-id.
INSERT INTO tags (tag, post_id) VALUES (:tag, :postid);

-- name: dissoc-tags!
-- Disassociate tags from post(id)
DELETE FROM tags 
WHERE post_id = :pid
AND tag IN (:tagnames);

-- name: delete-post-tags!
-- Delete post-tag-relation connected to the post id
DELETE FROM tags
WHERE tags.post_id = :pid;

-- name: all-relations
-- Get all post(title) - tag relations. !!FOR DEBUG!!
SELECT posts.title, tags.tag FROM posts,tags 
WHERE tags.post_id = posts.id
