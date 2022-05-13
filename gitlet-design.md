# Gitlet Design Document
Author: Ananya Bahugudumbi

## Classes and Data Structures


### Repository

Represents one Git repository, which manages the current commits, the staging process, trees, and so on.

#### Fields

1. ArrayList<Commit> commits: an array list of commits that keeps track of every commit thus far in chronological order.
2. ArrayList<File> files: every single file mentioned in the commits.

### Commit

Keeps track of the files given and the stage of saving they are at. 

#### Fields

1. String message: Message for human viewing ease that belongs to each commit, or a comment on the commit.
2. Date timestamp: a Date object that shows when the commit occurred.
3. String[] files: the files that are being updated in the commit.
4. String parent: a hashcode String or a Commit object name?? that is stored as a String object to preserve memory.

### Blob

Represents the text contained in a file at a given commit or point in time.

#### Fields

1. File f: The file that is being committed, used to serialize and compare different commits and adds.
2. String text: the text in file f (not sure if this is necessary yet).

## Algorithms


### Repository Class

####
The repository class keeps an ArrayList of commit objects and/or hashcode pointers for the sake of memory so that it can later reproduce a list of commits, beginning with the most recent. Additionally, the Repository class has an add method that uses similar logic to the commit class, in that it checks the contents of a blob to see if things have changed, and if they have, it creates a Blob object and makes the file's status as staged for committing. In this way, the add method prepares the computer to commit by gathering all of the necessary information, and when commit is called, the Commit object is created with the information gathered from add. Repository also has a remove method that removes a file from every commit, hence going through the ArrayList and deleting mentions of the file. The repository is initialized when the user calls the init method, and is initialized with one automatic commit that is always set to the same Date object and has no files. The repository method find also goes through the arraylist to find the commit message that matches its argument, which is a String. Status also gives the status of each file in the repository. It also takes care of branch and other commands pertaining to the tree-structure of the commits such as merge.

### Commit Class

####
A Commit object has a sameMessage(String m) method that checks if the message m is the same as the this's message. It also has getParent() which gives the parent Commit (as a Commit object rather than a pointer string). There's also addFiles, which adds files from the commit to the repository list if they're not already there and declares what the files states are (perhaps the ArrayList for files in the repository should be a hasmap indicating the state of the files?). 

### Blob Class

####
The Blob class is initialized by getting a serialization for the file and getting the text from the file and turning it into a single string, which is another instance variable. Blob has a method compare(Blob b) that compares the contents of this with b and if they are different it returns false, if they are the same it returns true.

####

##Persistence

When add occurs, the add method from the repository will be called. This add method will prepare for the commit and change the state, but it won't make any changes. Then, when commits happen, more data (aka a commit object) is added to the repository which makes it so that all of the data is repository is saved,but new data is being added. When implmenting remove et cetera, items other than the file indicated or the part indicated do not change.


