# TwitterEventtus
Eventtus programming problem

An objected oriented solution.  See rough UML diarams for overview.

This README will describe the choices, decisions, and designs.  It will address each class in the solution separately.  It will begin with and overview of the Activities, and then proceed to and examination of all the classes.

The solution comprises three screens and one Activity for each screen:  

1.  The MainActivity which can also be renamed as the LoginActivity, is the Activity associated with the first screen and handles user login requests.



2.  The ListOnLineFollowersActivity is the Activity associated with the second screen in the Application.  It appears immediately after logging in.  This Activity lists all the logged-in user’s followers who are also logged in to the app from their own devices.  Unfortunately, at this time, the Activity will list all the logged-in user’s followers (not just the ones logged in to the app from their own devices).  Although the Twitter API seems to have the necessary functionality to accomplish the former behavior, my testing showed that even though the functionality is documented, it has not yet been properly implemented.  See https://twittercommunity.com/t/get-all-users-logged-in-to-app-android/76035.  The code that checks whether a given follower has logged in to the app, is commented out at this point.

The list itself shows the user’s logged-in followers, including a the profile pic, username and  bio.

The Activity also has three buttons for additional functionality:
1.  The refresh button, which will refresh the list
2.  Switch language button, which will switch the language from English to Arabic and vice versa
3.  List users button, which will list the users that have recently used this app on this device and allow for the current user to switch to any of the users in the list without explicitly logging out and back in (if the user in the list has not already been logged out by Twitter).  The first user in this list is always the current user, and a selecting this list item is equivalent to logging out.

An useful smart phone UI design pattern (in my opinion) was utilized here.  The three aforementioned  buttons are not visible in this Activity by default.  Rather, they can be made to appear and disappear via an invisible button that is located along the far right edge of the smart phone screen.  The idea is to minimize clutter on the already tiny screen.  By utilizing this pattern, the screen appears to be larger (as opposed to always having a settings icon visible etc).



3.  The ViewFollowerTweetActivity is the Activity associated with the third screen in the Application.  It is launched upon selection of a logged-in follower from the list in second Activity.  This Activity shows the last 10 tweets of this user along with the user’s background graphic.




The non-Activity classes in the solution are:

1.  ConstantValues:  This contains constant values used by all the classes.



2.  MyApplication:  Application was extended in order to provide for a language switching experience at the application level that persisted through configuration changes.



3.  OnlineFollowersListKeeper: this class handled maintaining the list of the current user’s list of logged-in followers.  This class is instantiated by ListOnLineFollowersActivity and is a member field with a one to one mapping i.e. ListOnLineFollowersActivity has a OnlineFollowersListKeeper.  It is an active class i.e. runs in a separate thread of execution.  Every five minutes it will query Twitter regarding the current user’s logged in followers and make updates to the maintained list.  Also it exports a ‘force refresh’ method to force an immediate refresh.  This functionality is exploited by the refresh button of the ListOnLineFollowersActivity.

The list it maintains is a list of the User class.



4.  User:  This class represents a user as far as this Application is concerned.  It maintains exactly the fields required to be displayed in the ListOnLineFollowersActivity and the ViewFollowerTweetActivity, namely the full name, the user name, the bio, the url to the profile pic, the url to the background pic, and the user’s last 10 tweets.  It activity retrieves and saves the two pics to local storage since it has the data regarding urls.



5.  TwitterDataRetreiver:  This class creates the required http messages to retrieve the required JSON data according to Twitter’s specifications.  No third party libraries were used in order to keep things simple.  This class is instantiated by the OnlineFollowersListKeeper and is a member field with a one to one mapping i.e. OnlineFollowersListKeeper has a TwitterDataRetreiver;



6.  TwitterMediator:  This class consolidates all Twitter API calls, thereby localizing the impact of any changes by Twitter to one class.





Update:

With respect to the issue noted in the ListOnLineFollowersActivity section, regarding functionality that was documented but not yet unimplemented, this issue has been clarified. See the reply from Twitter Staff here https://twittercommunity.com/t/what-does-getsessionmap-do/76361/2 (I had to ask again but with a better looking question).  The answer, was that Twitter does not have this functionality.  And if one needs it, then it must be implemented using a custom backend.  The text in the javadocs has yet to be enriched to avoid confusion.  I have updated the project with a simple backend.