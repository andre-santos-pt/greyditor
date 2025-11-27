# Greyditor
Extensible grayscale editor for programming exercises with images.

<img width="547" height="355" alt="Screenshot 2025-11-14 at 11 44 48" src="https://github.com/user-attachments/assets/7a1cedc0-a1c3-4ce1-a0b1-b905a96e64f6" />


The following is a tutorial to understand how to use the editor with code compatible with Java 25 (using a compact file). To start programming with the editor, download the JAR file from [Releases](https://github.com/andre-santos-pt/greyditor/releases) and add it as a library to your project's classpath.

The full example can be found here: https://github.com/andre-santos-pt/greyditor/blob/main/src/pt/iscte/greyditor/examples/DemoCompactFile.java

Below you can find the minimal code to get the editor running. The image file has to be located at the execution directory. If calling simply *configuration.open()* the user may choose an image file through a dialog. 
```java
import pt.iscte.greyditor.*;

void main() {
    Greyditor configuration = new Greyditor("Demo");
    // configure features...
    configuration.open("monalisa.jpg");
}
```

## Filters
The simplest feature is an image filter, where each pixel tone is transformed uniformly. Filters are defined as a function that receives an integer (tone of a pixel) and returns a new tone. The following function illustrates a filter where each pixel tone is inverted. It is a on/off filter, without any variable aspect. 

```java
int invert(int tone) {
    return 255 - tone;
}
```

To include the filter in the editor, add it to the configuration, providing the text to be displayed in the filter's check box. When running the following configuration, a checkbox for activating the filter will appear.
```java
Greyditor configuration = new Greyditor("Demo");
// filter without parameter (check box)
configuration.addFilter("Invert", this::invert);

configuration.open("monalisa.jpg");
```

Filters may have an additional integer parameter for defining their intensity. The folowing function illustrates a filter for darkening the image given an *intensity*. The higher the value, the more dark the image gets.

```java
int darken(int tone, int intensity) {
    return Math.max(0, tone - intensity);
}
```

When adding filters with a parameter in the editor, one must specify the range of values for the argument. In this case, the value for *intensity* goes from zero (no effect) to 255 (maximum darkness). When running the following configuration, a slider for defining the filter intensity will appear.
```java
Greyditor configuration = new Greyditor("Demo");
// filter with parameter (slider)
configuration.addFilter("Darken", this::darken, 0, 255);

configuration.open("monalisa.jpg");
```

## Effects

An image effect transforms the image pixels in a non-uniform way, that is, not every pixel will be modified equally. As such, the implementation of an effect gains access to the whole matrix of pixels.

The following example is an effect that draws a grid of thirds over the image.

```java
void grid(int[][] image) {
    int hspace = (image[0].length + 2) / 3;
    int vspace = (image.length + 2) / 3;
    
    for (int y = vspace; y < image.length; y += vspace)
        for (int x = 0; x < image[y].length; x++)
            image[y][x] = 200;
    
    for (int x = hspace; x < image[0].length; x += hspace)
        for (int y = 0; y < image.length; y++)
            image[y][x] = 200;
}
```

The effect may be added to the editor as follows, similar to the inclusion of a filter.

```java
// effect without parameter (check box)
configuration.addEffect("Grid", this::grid);
```

As with filters, effects may have an integer parameter. The following effect adds horizontal black lines to the image with a variable *spacing*.

```java
void lines(int[][] image, int spacing) {
    if (spacing == 0)
        return;
    for (int y = 0; y < image.length; y += spacing)
        for (int x = 0; x < image[y].length; x++)
            image[y][x] = 0;
}
```

Effects with a parameter may be added to the editor in a similar way than their filter counterparts.

```java
// effect with parameter (slider)
configuration.addEffect("Lines", this::lines, 0, 50);
```

## Filters and effects as static methods
If the filters or effects are defined as static methods as illustrated below: 
```java
class Effects {
  static int darken(int tone, int intensity) {
    //...
  }

  static void lines(int[][] image, int spacing) {
    //...
  }
}
```

they can be included in the editor as follows:
```java
configuration.addFilter("darken", Effects::darken, 0, 255);
configuration.addEffect("lines", Effects::lines, 0, 50);
```

## Operations
While filters and effects act over the matrix of the image pixels, they cannot replace it (for instance, to obtain an image with a different size). Operations are the most flexible means because the image matrix may be replaced, if necessary. Operations are defined as a function that possibly returns the new matrix to be displayed in the editor. The following example illustrates an operation to square the image to the smallest dimension between width and height. 

```java
int[][] square(int[][] image) {
    int side = Math.min(image.length, image[0].length);
    int[][] square = new int[side][side];
    for (int y = 0; y < side; y++)
        for (int x = 0; x < side; x++)
            square[y][x] = image[y][x];

    return square;
}
```

An operation may be added to the editor as follows.

```java
// operation to square the image (button)
configuration.addOperation("Square", this::square);
```

The matrix that is returned replaces the editor image. If the function returns *null* the current image will not be replaced.


### Access to editor
An operation may access the editor to obtain the selected image region and perform user interaction. In this case, one should add an additional parameter of type *pt.iscte.greyeditor.Editor*. The following example is an operation to darken an area of the image.

```java
int[][] darkenArea(int[][] image, Editor editor) {
    Selection selection = editor.getSelection();
    if (selection == null) {
        editor.message("Please select an area of the image.");
    }
    else {
        int factor = editor.getInteger("Intensity?");
        for (int y = selection.y(); y < selection.y() + selection.height(); y++)
            for (int x = selection.x(); x < selection.x() + selection.width(); x++)
                image[y][x] = Math.max(0, image[y][x] - factor);
    }
    return null;
}
```

The *Selection* object describes the area that is currently selected. If *null* means there is no selection. Otherwise, (*x*, *y*) provides the top-left corner of the selection, while *width* and *height* hold the dimension of the selection in pixels. 


### Default operations
The editor has built-in default operations for loading and saving images. These may be included as follows.

```java
configuration.addLoadOperation("Load");
configuration.addSaveOperation("Save");
```

### Operations as class methods
If the operations are defined in classes as illustrated below: 
```java
class Operations {
  int[][] square(int[][] image) {
    //...
  }
  int[][] darkenArea(int[][] image, Editor editor) {
    //...
  }
}
```
they can be included in the editor as follows:
```java
Operations operations = new Operations();
configuration.addOperation("square", operations::square);
configuration.addOperation("darken", operations::darkenArea);
```

