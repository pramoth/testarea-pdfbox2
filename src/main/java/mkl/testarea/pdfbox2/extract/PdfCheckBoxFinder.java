package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

/**
 * <a href="https://stackoverflow.com/questions/64093610/extract-checkbox-value-out-of-pdf-1-7-using-pdfbox">
 * Extract Checkbox value out of PDF 1.7 using PDFBox
 * </a>
 * <br/>
 * <a href="https://drive.google.com/file/d/12O9W3SE4l4EZg7WArYoL3e6M579I7w3U/view?usp=sharing">
 * Updated_Form.pdf
 * </a>
 * <p>
 * This stream engine class detects checked and un-checked check boxes
 * constructed exactly as in the OP's example file.
 * </p>
 * <p>
 * For a given {@link PDPage} <code>page</code> use this class like this:
 * </p>
 * <pre>
 * PdfCheckBoxFinder boxFinder = new PdfCheckBoxFinder(page);
 * boxFinder.processPage(page);
 * </pre>
 * <p>
 * Thereafter you can inspect <code>boxFinder.getBoxes()</code>.
 * </p>
 * @author mkl
 */
public class PdfCheckBoxFinder extends PDFGraphicsStreamEngine {
    public class CheckBox {
        public Point2D getLowerLeft()   {   return lowerLeft;   }
        public Point2D getUpperRight()  {   return upperRight;  }
        public boolean isChecked()      {   return checked;     }

        CheckBox(Point2D lowerLeft, Point2D upperRight, boolean checked) {
            this.lowerLeft = lowerLeft;
            this.upperRight = upperRight;
            this.checked = checked;
        }

        final Point2D lowerLeft;
        final Point2D upperRight;
        final boolean checked;
    }

    public PdfCheckBoxFinder(PDPage page) {
        super(page);
        for (int i = 0; i < pathAnchorsByType.length; i++)
            pathAnchorsByType[i] = new ArrayList<Point2D>();
    }

    public List<CheckBox> getBoxes() {
        if (checkBoxes.isEmpty()) {
            for (Point2D anchor : pathAnchorsByType[PathType.boxBottom.index]) {
                if (containsApproximatly(pathAnchorsByType[PathType.boxLeft.index], anchor) &&
                        containsApproximatly(pathAnchorsByType[PathType.boxRight.index], anchor) &&
                        containsApproximatly(pathAnchorsByType[PathType.boxTop.index], anchor)) {
                    Point2D upperRight = new Point2D.Float(7.5f + (float)anchor.getX(), 7.5f + (float)anchor.getY());
                    boolean checked = containsInRectangle(pathAnchorsByType[PathType.checkLeft.index], anchor, upperRight) &&
                            containsInRectangle(pathAnchorsByType[PathType.checkRight.index], anchor, upperRight);
                    checkBoxes.add(new CheckBox(anchor, upperRight, checked));
                }
            }
        }
        return Collections.unmodifiableList(checkBoxes);
    }

    boolean containsApproximatly(List<Point2D> points, Point2D anchor) {
        for (Point2D point : points) {
            if (approximatelyEquals(point.getX(), anchor.getX()) && approximatelyEquals(point.getY(), anchor.getY()))
                return true;
        }
        return false;
    }

    boolean containsInRectangle(List<Point2D> points, Point2D lowerLeft, Point2D upperRight) {
        for (Point2D point : points) {
            if (lowerLeft.getX() < point.getX() && point.getX() < upperRight.getX() &&
                    lowerLeft.getY() < point.getY() && point.getY() < upperRight.getY())
                return true;
        }
        return false;
    }

    //
    // PDFGraphicsStreamEngine overrides
    //
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        moveTo((float) p0.getX(), (float) p0.getY());
        path.add(new Rectangle(p0, p1, p2, p3));
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        currentPoint = new Point2D.Float(x, y);
        currentStartPoint = currentPoint;
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        Point2D point = new Point2D.Float(x, y);
        path.add(new Line(currentPoint, point));
        currentPoint = point;
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        Point2D point1 = new Point2D.Float(x1, y1);
        Point2D point2 = new Point2D.Float(x2, y2);
        Point2D point3 = new Point2D.Float(x3, y3);
        path.add(new Curve(currentPoint, point1, point2, point3));
        currentPoint = point3;
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return currentPoint;
    }

    @Override
    public void closePath() throws IOException {
        path.add(new Line(currentPoint, currentStartPoint));
        currentPoint = currentStartPoint;
    }

    @Override
    public void endPath() throws IOException {
        clearPath();
    }

    @Override
    public void strokePath() throws IOException {
        clearPath();
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        processPath();
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {
        clearPath();
    }

    @Override public void drawImage(PDImage pdImage) throws IOException { }
    @Override public void clip(int windingRule) throws IOException { }
    @Override public void shadingFill(COSName shadingName) throws IOException { }

    //
    // internal representation of a path
    //
    interface PathElement {
    }

    class Rectangle implements PathElement {
        final Point2D p0, p1, p2, p3;

        Rectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }
    }

    class Line implements PathElement {
        final Point2D p0, p1;

        Line(Point2D p0, Point2D p1) {
            this.p0 = p0;
            this.p1 = p1;
        }
    }

    class Curve implements PathElement {
        final Point2D p0, p1, p2, p3;

        Curve(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }
    }

    Point2D currentPoint = null;
    Point2D currentStartPoint = null;

    void clearPath() {
        path.clear();
        currentPoint = null;
        currentStartPoint = null;
    }

    void processPath() {
        for (PathType pathType : PathType.values()) {
            if (pathType.matches(path)) {
                pathAnchorsByType[pathType.index].add(pathType.getAnchor(path));
            }
        }

        clearPath();
    }

    enum PathType {
        boxTop(new float[] {7.5f, 0f, .75f, .75f, -9f, 0f, .75f, -.75f}, new float[] {0f, -7.5f}, 0),
        boxRight(new float[] {0f, -7.5f, .75f, -.75f, 0f, 9f, -.75f, -.75f}, new float[] {-7.5f, -7.5f}, 1),
        boxBottom(new float[] {-7.5f, 0f, -.75f, -.75f, 9f, 0f, -.75f, .75f}, new float[] {-7.5f, 0f}, 2),
        boxLeft(new float[] {0f, 7.5f, -.75f, .75f, 0f, -9f, .75f, .75f}, new float[] {0f, 0f}, 3),
        checkRight(new float[] {-2.65165f, -2.65165f, 0f, -1.06066f, 3.18198f, 3.18198f, -.53033f, .53033f}, new float[] {-2.65165f, -2.65165f/*-5.1072f, -4.4559f*/}, 4),
        checkLeft(new float[] {-1.06066f, 1.06066f, -.53033f, -.53033f, 1.59099f, -1.59099f, 0f, 1.06066f}, new float[] {0f, 0f/*-2.4556f, -1.8042f*/}, 5)
        ;
        PathType(float[] diffs, float[] offsetToAnchor, int index) {
            this.diffs = diffs;
            this.offsetToAnchor = offsetToAnchor;
            this.index = index;
        }

        boolean matches(List<PathElement> path) {
            if (path != null && path.size() * 2 == diffs.length) {
                for (int i = 0; i < path.size(); i++) {
                    PathElement element = path.get(i);
                    if (!(element instanceof Line))
                        return false;
                    Line line = (Line) element;
                    if (!approximatelyEquals(line.p1.getX() - line.p0.getX(), diffs[i*2]))
                        return false;
                    if (!approximatelyEquals(line.p1.getY() - line.p0.getY(), diffs[i*2+1]))
                        return false;
                }
                return true;
            }
            return false;
        }

        Point2D getAnchor(List<PathElement> path) {
            if (path != null && path.size() > 0) {
                PathElement element = path.get(0);
                if (element instanceof Line) {
                    Line line = (Line) element;
                    Point2D p = line.p0;
                    return new Point2D.Float((float)p.getX() + offsetToAnchor[0], (float)p.getY() + offsetToAnchor[1]);
                }
            }
            return null;
        }

        final float[] diffs;
        final float[] offsetToAnchor;
        final int index;
    }

    static boolean approximatelyEquals(double f, double g) {
        return Math.abs(f - g) < 0.001;
    }

    //
    // members
    //
    final List<PathElement> path = new ArrayList<>();

    final List<Point2D>[] pathAnchorsByType = new List[PathType.values().length];

    final List<CheckBox> checkBoxes = new ArrayList<>(); 
}
