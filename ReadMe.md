## Udacity P2 project - Movie App ##

### See assignment details at ###
https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.ngb5kvi0pkbz

### Description of work ###

Movie app project 2 is  continuous work of project 1, which was created from scratch. Highlights of this project:
* Added Sqlite3 DB for data persistence.
* Added ContentProvider over DB storage.
* Replaced AsyncTask with SyncAdapter that periodically update the data layer. Manual refresh when data is day or more old.   
* Added 2 panes, master and detail side by side, for wide screen.
* Different portrait and landscape screen orientations.
* Replace GridView with RecyclerView. The RecyclerView has GridLayoutManager
* Added Movie Trailer video viewing if YouTube is available.
* The videos and reviews are retrieved on-demand. Once the items are retried, they are stores in database

### Special Notes ###
* You need to acquire an API key from https://www.themoviedb.org/. Then add the API key to gradle.properties file by adding the following line:
   `themoviedbApiKeyProp="your api key"`
* Citation: the code utilizes https://www.themoviedb.org/ APIs, copyrights of themoviewdb.org.

### Known Issues and TODOs ###
* The first launch is very slow. once the data base is filled, it works better.
   --Note: if the movie thumb nails are not shown, try re-launch it.--
* TODO: disable the Favorite page if it is empty
* TODO: in two pane screen mode, display first movie details when first start.
* TODO: High light the selected movie at main activity.

### Screenshots ###
![land-two-pane](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-tablet-land.png?raw=true)
![port-two-pane](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-tablet-port-favor.png?raw=true)
![land-main](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-main-land.png?raw=true)
![land-detail](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-detail-land.png?raw=true)
![port-main](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-main-port.png?raw=true)
![port-detail-favorite](https://github.com/mingrutar/movieApp/blob/master/screenShorts/P2-detail-port.png?raw=true)
