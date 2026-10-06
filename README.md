# FileTree

## 1 Overview
FileTree produces tree-structured graphics from a given point of the file system as below.

```
📁 FileTree
├─📁 build
│  └─📁 libs
├─📁 src
│  └─📁 main
│     ├─📁 java
│     └─📁 resources
└─📄 build.gradle
```

Further, you can edit the tree in various way. For example, you can:
* Change file names;
* hi-light text by boldface or color;
* change the order of directories and files; and
* remove unnecessary tree node

Finally, you can file the resulting tree as Unicode/ASCII tree text file so that you can incorporate it in your document to make it rich and colorful.

## 2 How to run
FileTree is delivered as Java jar file, so you have to install JDK 21 or later in advance.

In the system JDK 21 or later is available, you enter the following from your shell window.

```
$ java -jar /<path-to-jar-file>/FileTree.jar
```
Otherwise, in many cases, all you have to do is simply double-click FileTree.jar icon.

## 3 User Interface
### 3.1 Main Windows

|Item|Description|
|-------|-------------------------|
| [Base Directory] button | File chooser is shown for you to choose a base directory of the filesystem.|
| Base directory text field | The base directory is show. This field can work as drag & drop target from file browser.|
| [Make] button|Generates a file tree in the tree pane below.|
|[File] button| Shows File dialog for you to file the tree as text file.|
|tree-manipulation-handle|You can expand and collapse any point of the tree by clicking the handle icon.|
|context-menu|Right-clicking of an item will show up context-menu for you to modify the tree.|

### 3.2 File dialog window
This dialog window controls how the tree is redered as text file.

|Item|Description|
|----|----|
| [Text Tree File] button | File chooser is shown for you to choose a file to write the tree text.|
| Text Tree File text field | Text Tree File text file is show. This field can work as drag & drop target from file browser.|
|Unicode|Unicode Box Drawing characters, └├│─, will be used to render tree.|
|ASCII|ASCII characters, +-\|, will be used to render tree.|
|Unicode Icon|Unicode file icons will be used to make the tree fancy.|
|Directory suffix(/)|'/' will be suffixed after directory name.|
|Spacing|Controls spaces leading before vertical line, trailing after vertical line, before icon and before file name.|
|\<html\> ... \</html\>|Encloses the tree text by html envelop. You can preview the result by your favorite browser.|
|\<pre\> ... \</pre\>|Encloses the tree text by pre-formatted envelop.|
|[File] button|Writes the tree in the specified file above.|
|[Close]|Closes the dialog|

