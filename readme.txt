
1) package com.squareup.picasso(picasso) for loading images
2) API https://www.themoviedb.org/ for movie info
3) my themoviedb api_key=cdf5f229abf9f31735694c38c48a67ac
  https://api.themoviedb.org/3/movie/550?api_key=cdf5f229abf9f31735694c38c48a67ac
15% coupon for Udacity: UM2JHZM1DFQ9WWT
---working for movie id=550:
1) https://api.themoviedb.org/3/movie/550?api_key=cdf5f229abf9f31735694c38c48a67ac  //desc with 1 image
2) all images
req=https://api.themoviedb.org/3/movie/550/images?api_key=cdf5f229abf9f31735694c38c48a67ac&language=en&include_image_language=en,null
req={"id":550,"backdrops":[{"aspect_ratio":1.77777777777778,"file_path":"/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg","height":720,"iso_639_1":"en","vote_average":5.39153439153439,"vote_count":27,"width":1280},{"aspect_ratio":1.77777777777778,"file_path":"/hNFMawyNDWZKKHU4GYCBz1krsRM.jpg","height":720,"iso_639_1":"xx","vote_average":5.3639846743295,"vote_count":24,"width":1280},{"aspect_ratio":1.77777777777778,"file_path":"/87hTDiay2N2qWyX4Ds7ybXi9h8I.jpg","height":1080,"iso_639_1":null,"vote_average":5.35181236673774,"vote_count":4,"width":1920} }
3) get image:
http://image.tmdb.org/t/p/w1280/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg   // .jpg get from above

reference manual http://docs.themoviedb.apiary.io/#reference/authentication/authenticationsessionnew
base= https://api.themoviedb.org/3/    //3=version 3;
actions, at left panel of ref manual, account, list, movie, etc

req=https://api.themoviedb.org/3/authentication/guest_session/new?api_key=cdf5f229abf9f31735694c38c48a67ac
res= {"success":true,"guest_session_id":"5afb94e961a9486bc83658add2be6521","expires_at":"2016-05-04 22:19:03 UTC"}
   //guest can rate
https://api.themoviedb.org/3/certification/movie/list?api_key=cdf5f229abf9f31735694c38c48a67ac

// get certification definitions all countries, such as R, PG-13...
https://api.themoviedb.org/3/certification/movie/list?api_key=cdf5f229abf9f31735694c38c48a67ac

https://api.themoviedb.org/3/discover/movie?primary_release_date.gte=2016-03-20&primary_release_date.lte=2016-05-03&api_key=cdf5f229abf9f31735694c38c48a67ac
https://api.themoviedb.org/3/?api_key=cdf5f229abf9f31735694c38c48a67ac
------------
NO https://api.themoviedb.org/3/movie/
https://api.themoviedb.org/3/movie/550/images?api_key=cdf5f229abf9f31735694c38c48a67ac&language=en&include_image_language=en,null

https://api.themoviedb.org/discover/movie?sort_by=popularity.desc
&api_key=cdf5f229abf9f31735694c38c48a67ac



req= https://api.themoviedb.org/3/movie/18/images?api_key=cdf5f229abf9f31735694c38c48a67ac&language=en&include_image_language=en,null

https://api.themoviedb.org/collection/3/movie?api_key=cdf5f229abf9f31735694c38c48a67ac
/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg


