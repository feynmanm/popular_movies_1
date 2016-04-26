To run this app, you need to set up an account on themoviedb.org and request and receive an API key. In the DataFetcher.java file, assign your themoviedb.org API key (as a string literal) that you requested from that site to the API_KEY static variable:
public final static String API_KEY = "XXX";

Couple Notes:
As a learning exercise, I wrote the ContentProvider from scratch instead of using a library (though it's a very simple database).
I also did not employ a service for the API calls to themoviedb.org and ContentProvder queries because I wanted to muscle through an AnsycTask for practice.
