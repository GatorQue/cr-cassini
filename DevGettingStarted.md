# Introduction #

To get started you must first download and install the following programs:
  * Android Development Toolkit (http://developer.android.com/sdk/index.html#win-bundle)
  * Mercurial Client (http://tortoisehg.bitbucket.org/)

# Details #

I installed the ADT in my G:\Dev folder as G:\Dev\ADT. From this folder you can execute the G:\Dev\ADT\eclipse\eclipse.exe file to launch the Eclipse IDE. But before you do that you should download the source code by doing the following:
  * Open a Command Prompt window and navigate to the G:\Dev folder (cd G:\Dev)
  * Type the following in the Command Prompt window to download the latest source code for this project:
  * hg clone https://code.google.com/p/cr-cassini/
  * Close the Command Prompt window
After downloading the source code you will also need the assets file (images, sounds, etc).  You can retrieve these from the Google Drive link sent to you earlier. After downloading the assets-YYYYMMDD.zip file, extract it into the cr-cassini/cassini-android folder where it will create the assets directory and all its sub directories. Always try and use the latest version of the assets zip file when possible.

Now we can finally import the source code into Eclipse and run and edit the project.  Open Eclipse using the path mentioned above. After launching Eclipse and closing the Welcome screen do the following to import the project:
  * Click File->Import
  * In the General folder select Existing Projects into Workspace
  * Click Next
  * Click the Browse button next to "Select root directory" and navigate to the G:\Dev\cr-cassini folder cloned above.
  * Select all the projects listed and make sure the "Copy projects into Workspace" option is **NOT** checked
  * Click Finish to complete the process.
Now you should see the **cassini**, **cassini-android**, **cassini-desktop**, and **cassini-html** projects listed. The **cassini** project is where the core game code development occurs. The other projects are shell projects for specific platforms.  Right now the html platform doesn't work because you are missing the GWT toolkit libraries and because of some code incompatibilities that I need to fix.

To run the Desktop platform you must select the **cassini-desktop** project and click the Run As or Debug As icon. The same can be said about the **cassini-android** project as well. It helps to plug in your Android device first before trying to run the **cassini-android** project.

As source code changes get posted you can do the following:
  * From a Windows Explorer window, right click on the cr-cassini folder and select Hg Workbench.
  * From the HG Workbench you can click the "Pull incoming changes from selected URL" icon to pull in the code changes.

To post source code changes you must first "commit" your changes and then "pull" the latest changes using Hg Workbench. As you "pull" the latest changes you will be given the chance to merge your changes with those that you "pull".  After merging and testing the changes together go ahead and "push" your changes back to the Google Code repository. Try to "pull" and "push" small changes frequently to make it easier for others to keep in sync with your changes.