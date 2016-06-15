Udacity P2 project Movie App

See assignment details at
   https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.ngb5kvi0pkbz

ToDO:
   Replace the value of 'themoviedbApiKKeyProp' in gradle.properties.

Screenshots for P2:
  /screenShorts/p2-device-xxxxx.jpg

The project uses ContentProvider, Sqlite3 helper and SyncAdapter. Images are pulled by Picasso. The other data are retrieved via SyncData. When app starts, the sync is called when last sync is > 1 day.
The UI main page uses ViewPager with 3 Fragments, each contains a Grid that holds one of  Popular, Top-Rated and Favorite list. The Grid is a RecyclerView with GridLayoutManager. 
The videos and reviews are retrieved on-demand. Once the items are retried, they are stores in database. 

The Known Issue:
1) When first starts, the thumbnails will not shown. WORKAROUND: you can change device orientation or select another page to make it display.
2) Related to above, the detail page in 'tow pane' mode does not synch initially.
3) Note: The first launch is very slow. once the data base is filled, it works better.
4) the Favorite starts with a empty list, for now, it is a blank screen. 

The first two issues are related. The problem is that the RecyclerViewAdapter does not always notify the View when notifyDataSetChanged() is called. Whatever solutions I tried only gets the matter worse.

 Note: I use RecyclerViewAdapter with CursorLoader for the thumnails.
 Somehow the Adapter.notifyDataSetChanged() does not trig RecyclerView update.
 I have tried for days and encouterated crach and burns, but did not solve the problem.
 QUESTION: what are the proper ways to solve this issue?
