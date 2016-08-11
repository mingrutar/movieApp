PRAGMA foreign_keys = ON;
CREATE TABLE movie (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   movie_id INTEGER NOT NULL UNIQUE,
   title VARCHAR NOT NULL,
   release_date INTEGER NOT NULL,
   poster_path VARCHAR NOT NULL,
   vote_average REAL NOT NULL,
   vote_count INTEGER NOT NULL,
   popularuty REAL NOT NULL,
   overview TEXT NOT NULL,
   original_title VARCHAR,
   original_language VARCHAR,
   backdrop_path VARCHAR
  );
CREATE TABLE detail (
   _id   INTEGER PRIMARY KEY AUTOINCREMENT,
   movie_id REFERENCES movie(_id) ON DELETE CASCADE,
   type VARCHAR(10) NOT NULL,
   data TEXT NOT NULL,
   UNIQUE (movie_id, type ) ON CONFLICT REPLACE
);
CREATE TABLE IF NOT EXISTS movie_selection (
   _id   INTEGER PRIMARY KEY AUTOINCREMENT,
   movie_id REFERENCES movie(_id) ON DELETE CASCADE,
   selection_type INTEGER NOT NULL,
   UNIQUE (selection_type, movie_id) ON CONFLICT REPLACE
 );
CREATE VIEW popular_movie AS
SELECT m.*
FROM movie m INNER JOIN movie_selection AS ms
   ON  m._id = ms.movie_id and ms.selection_type=0
ORDER BY ms._id ;
CREATE VIEW top_rated_movie AS
SELECT m.*
FROM movie m, movie_selection ms
WHERE m._id = ms.movie_id and ms.selection_type=1
ORDER BY ms._id ;
CREATE VIEW favorite_movie AS
SELECT m.*
FROM movie m, movie_selection ms
WHERE m._id = ms.movie_id and ms.selection_type=2
ORDER BY ms._id ;
