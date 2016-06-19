Udacity P2 project MovieApp

See assignment details at
   https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.ngb5kvi0pkbz

Citation: the code utilizes https://www.themoviedb.org/ APIs, copyrights of themoviewdb.org.

Please add this line to gradle.properties:
  themoviedbApiKeyProp="your api key"

Screenshots for P2:
  /screenShorts/p2-device-xxxxx.jpg

The project uses ContentProvider, Sqlite3 helper and SyncAdapter. Images are pulled by Picasso. The other data are retrieved via SyncData. When app starts, the sync is called when last sync is > 1 day.
The UI main page uses ViewPager with 3 Fragments, each contains a Grid that holds one of  Popular, Top-Rated and Favorite list. The Grid is a RecyclerView with GridLayoutManager. 
The videos and reviews are retrieved on-demand. Once the items are retried, they are stores in database. 

The Known Issue:
1) The first launch is very slow. once the data base is filled, it works better.
   Note: if the screen does not show, try re-launch it.
2) the Favorite starts with a empty list, for now, it is a blank screen.
