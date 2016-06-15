Udacity P2 project Movie App

See assignment details at
   https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.ngb5kvi0pkbz

ToDO:
   Replace the value of 'themoviedbApiKKeyProp' in gradle.properties.

Screenshots for P2:
  /screenShorts/p2-device-xxxxx.jpg

Know issues:
1) first rime, the thumnils will not refresh.
   WORKAROUND Options:
      1) change page selection from spinner menu. it will refresh
      2) change device oritation
2) related to above, the detail page in 'tow pane' mode does not synch initially.

 Note: I use RecyclerViewAdapter with CursorLoader for the thumnails.
 Somehow the Adapter.notifyDataSetChanged() does not trig RecyclerView update.
 I have tried for days and encouterated crach and burns, but did not solve the problem.
 QUESTION: what are the proper ways to solve this issue?
