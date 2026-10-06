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
Otherwise, in most cases, all you have to do is simply double-click FileTree.jar icon.

## 3 User Interface
### 3.1 Main Windows

|Item|Description|
|-------|-------------------------|
|<button type="button">Base Directory</button>| File chooser is shown for you to choose a base directory of the filesystem.|
| <input  type="text" value="Base/File/Path"></input> | The base directory is show. This field can work as drag & drop target from file browser.|
|<button type="button">Make</button>|Generates a file tree in the tree pane below.|
|<button type="button">File</button>| Shows File dialog for you to file the tree as text file.|
|tree-manipulation-handle|You can expand and collapse any point of the tree by clicking the handle icon.|
|context-menu|Right-clicking of an item will show up context-menu for you to modify the tree.|

### 3.2 File dialog window
This dialog window controls how the tree is redered as text file.

|Item|Description|
|----|----|
|<button type="button">Text Tree File</button>| File chooser is shown for you to choose a file to write the tree text.|
| <input  type="text" value="Text/Tree/File/Path"></input>| Text Tree File text file is show. This field can work as drag & drop target from file browser.|
|<input  type="radio" checked="">Unicode</input>|Unicode Box Drawing characters, └├│─, will be used to render tree.|
|<input  type="radio">ASCII</input>|ASCII characters, +-\|, will be used to render tree.|
|<input  type="checkbox" checked="">Unicode Icon</input>|Unicode file icons will be used to make the tree fancy.|
|<input  type="checkbox" value="TRUE">Directory suffix(/)</input>|'/' will be suffixed after directory name.|
|Spacing|Controls spaces leading before vertical line, trailing after vertical line, before icon and before file name.|
|<input  type="checkbox" checked="true"></input>\<html\> ... \</html\>|Encloses the tree text by html envelop. You can preview the result by your favorite browser.|
|<input  type="checkbox"></input>\<pre\> ... \</pre\>|Encloses the tree text by pre-formatted envelop.|
|<button type="button">File</button>|Writes the tree in the specified file above.|
|<button type="button">Close</button>|Closes the dialog|

