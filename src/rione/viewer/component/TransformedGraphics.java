package rione.viewer.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;

/**
 * 座標変換をした描画を行うGraphics2DのDecorator
 * 
 * @author utisam
 * 
 */
public class TransformedGraphics {

	final private Graphics2D g;
	final private ScreenTransform t;

	/**
	 * tにより変換を行いながら，gに対して描画を行うTransformedGraphicsを生成します．
	 * @param g 描画対象となるGraphics2D
	 * @param t 座標変換を行うScreenTransform
	 */
	public TransformedGraphics(Graphics2D g, ScreenTransform t) {
		this.g = g;
		this.t = t;
	}

	/**
	 * x, y, w, hを変換して矩形を描画します．
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void clearRect(int x, int y, int width, int height) {
		g.clearRect(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height));
	}

	/**
	 * x, y, w, h を変換してコピーします．
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		g.copyArea(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height), dx, dy);
	}

	/**
	 * sを変換して描画します．
	 * 
	 * @param s
	 */
	public void draw(Shape s) {
		g.draw(transform(s));
	}
	
	/**
	 * x, y, w, hを変換し，円弧または楕円弧を塗りつぶします．<br>
	 * 角度は右向き水平方向を0度，右上隅を45度とします．
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param startAngle 開始角度
	 * @param arcAngle 展開角度
	 */
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.drawArc(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height), startAngle, arcAngle);
	}

	/**
	 * x, yを変換して線を引きます．
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		g.drawLine(t.xToScreen(x1), t.yToScreen(y1), t.xToScreen(x2),
				t.yToScreen(y2));
	}

	/**
	 * x, y, w, hを変換して楕円を描画します．
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawOval(int x, int y, int width, int height) {
		g.drawOval(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height));
	}

	/**
	 * xPoints, yPointsを変換して描画します<br>
	 * パスは閉じて描画されます．
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Path2D poly = transformedPolygon(xPoints, yPoints, nPoints);
		poly.closePath();
		g.fill(poly);
	}

	/**
	 * xPoints, yPointsを変換して描画します．<br>
	 * パスは閉じられません．
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		g.draw(transformedPolygon(xPoints, yPoints, nPoints));
	}

	/**
	 * x, y, w, hを変換して矩形を描画します．
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawRect(int x, int y, int width, int height) {
		g.drawRect(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height));
	}

	/**
	 * x, y, w, hを変換して角の丸い矩形を描画します．
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g.drawRoundRect(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height), arcWidth, arcHeight);
	}

	/**
	 * x, yを変換し，AttributedCharacterIteratorを描画します．
	 * @param iterator
	 * @param x
	 * @param y
	 */
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		g.drawString(iterator, t.xToScreen(x), t.yToScreen(y));
	}

	/**
	 * x, yを変換し，AttributedCharacterIteratorを描画します．
	 * @param iterator
	 * @param x
	 * @param y
	 */
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		g.drawString(iterator, t.xToScreen(x), t.yToScreen(y));
	}

	/**
	 * x, yを変換し，Stringを描画します．
	 * @param str
	 * @param x
	 * @param y
	 */
	public void drawString(String str, float x, float y) {
		g.drawString(str, t.xToScreen(x), t.yToScreen(y));
	}

	/**
	 * x, yを変換し，Stringを描画します．
	 * @param str
	 * @param x
	 * @param y
	 */
	public void drawString(String str, int x, int y) {
		g.drawString(str, t.xToScreen(x), t.yToScreen(y));
	}

	/**
	 * Shapeを変換し描画します．
	 * @param s
	 */
	public void fill(Shape s) {
		g.fill(transform(s));
	}

	/**
	 * x, y, w, hを変換し，円弧または楕円弧を塗りつぶします．
	 * 角度は右向き水平方向を0度，右上隅を45度とします．
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param startAngle 開始角度
	 * @param arcAngle 展開角度
	 */
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.fillArc(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height), startAngle, arcAngle);
	}

	/**
	 * x, y, w, hを変換して楕円を塗りつぶします．
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillOval(int x, int y, int width, int height) {
		g.fillOval(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height));
	}

	/**
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Path2D poly = transformedPolygon(xPoints, yPoints, nPoints);
		poly.closePath();
		g.fill(poly);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height));
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g.fillRoundRect(t.xToScreen(x), t.yToScreen(y), t.xToScreen(width),
				t.yToScreen(height), arcWidth, arcHeight);
	}

	/**
	 * g.setColorと同様です．
	 * @param c
	 */
	public void setColor(Color c) {
		g.setColor(c);
	}

	/**
	 * ストロークを設定する<br>
	 * 区別するのがめんどくさい人向け
	 * 
	 * @param s
	 */
	public void setStroke(Stroke s) {
		g.setStroke(s);
	}

	/**
	 * BasicStrokeさえめんどくさくなった人向け<br>
	 * 
	 * @param thickness
	 */
	public void setThickness(float thickness) {
		g.setStroke(new BasicStroke(thickness));
	}

	private Shape transform(Shape s) {
		Path2D result = new Path2D.Double();
		PathIterator it = s.getPathIterator(null);
		double[] c = new double[6];
		while (!it.isDone()) {
			switch (it.currentSegment(c)) {
			case PathIterator.SEG_MOVETO:
				result.moveTo(t.xToScreen((int) c[0]), t.yToScreen((int) c[1]));
				break;
			case PathIterator.SEG_LINETO:
				result.lineTo(t.xToScreen((int) c[0]), t.yToScreen((int) c[1]));
				break;
			case PathIterator.SEG_QUADTO:
				result.quadTo(t.xToScreen((int) c[0]), t.yToScreen((int) c[1]),
						t.xToScreen((int) c[2]), t.yToScreen((int) c[3]));
				break;
			case PathIterator.SEG_CUBICTO:
				result.curveTo(t.xToScreen((int) c[0]),
						t.yToScreen((int) c[1]), t.xToScreen((int) c[2]),
						t.yToScreen((int) c[3]), t.xToScreen((int) c[4]),
						t.yToScreen((int) c[5]));
				break;
			case PathIterator.SEG_CLOSE:
				result.closePath();
				break;
			}
			it.next();
		}
		return result;
	}

	private Path2D transformedPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Path2D poly = new Path2D.Double();
		poly.moveTo(t.xToScreen(xPoints[0]), t.yToScreen(xPoints[0]));
		for (int i = 1; i < nPoints; i++) {
			poly.lineTo(t.xToScreen(xPoints[i]), t.yToScreen(xPoints[i]));
		}
		return poly;
	}

	/**
	 * 行番号を指定して左端に描画する
	 * 
	 * @param string
	 * @param line
	 */
	public void drawStringWithLine(String string, int line) {
		g.drawString(string, 16, 80 + line * g.getFontMetrics().getAscent());
	}

	/**
	 * 行番号を指定して描画する
	 * 
	 * @param string
	 * @param x
	 *            Xのオフセット
	 * @param y
	 *            Yのオフセット
	 * @param line
	 */
	public void drawStringWithLine(String string, int x, int y, int line) {
		g.drawString(string, t.xToScreen(x), line
				* g.getFontMetrics().getAscent() + t.yToScreen(y));
	}

	/**
	 * screen座標系
	 */
	public void drawTextAndBox_Screen(	int x, int y, ArrayList<String> list, Color Color_String,
			Color Color_Box, Color Color_Edge) {
		int line = list.size();
		String max = "";
		for (String str : list) {
			if (str.length() > max.length()) {
				max = str;
			}
		}
		int fontHeight = g.getFontMetrics().getAscent();
		int fontWidth = g.getFontMetrics().stringWidth(max) / max.length();
		g.setColor(Color_Box);
		g.fillRect(x - fontWidth / 2, y - fontHeight, g.getFontMetrics()
				.stringWidth(max) + fontWidth, line * fontHeight + fontHeight
				/ 2);
		line = 0;
		g.setColor(Color_String);
		for (String str : list) {
			g.drawString(str, x, y + line * fontHeight
					+ g.getFontMetrics().getLeading());
			line++;
		}
		g.setColor(Color_Edge);
		g.setStroke(new BasicStroke(3));
		g.drawRect(x - fontWidth / 2, y - fontHeight, g.getFontMetrics()
				.stringWidth(max) + fontWidth, line * fontHeight + fontHeight
				/ 2);
		g.setStroke(new BasicStroke(1));
	}

	/**
	 * screen座標系
	 * */
	public void drawTextAndBox_Screen(int x, int y, ArrayList<String> list, Color Color_String,
			Color Color_Box) {
		drawTextAndBox_Screen(x, y, list, Color_String, Color_Box,
				Color_Box);
	}

	/**
	 * sim座標系
	 */
	public void drawTextAndBox_sim(int x, int y, ArrayList<String> list, Color Color_String,
			Color Color_Box, Color Color_Edge) {
		drawTextAndBox_Screen(t.xToScreen(x), t.yToScreen(y), list,
				Color_String, Color_Box, Color_Edge);
	}

	/**
	 * sim座標系
	 */
	public void drawTextAndBox_sim(int x, int y, ArrayList<String> list, Color Color_String,
			Color Color_Box) {
		drawTextAndBox_sim(x, y, list, Color_String, Color_Box, Color_Box);
	}

	public void drawTweet(StandardEntity se, String str) {
		int ScreenBaseX = 0;
		int ScreenBaseY = 0;
		String flag = "default";
		Color typeColor = Color.white;
		try {
			switch (se.getStandardURN()) {
			case AMBULANCE_TEAM:
				ScreenBaseX = t.xToScreen(((Human) se).getX());
				ScreenBaseY = t.yToScreen(((Human) se).getY());
				typeColor = Color.gray;
				flag = "AT";
				break;
			case FIRE_BRIGADE:
				ScreenBaseX = t.xToScreen(((Human) se).getX());
				ScreenBaseY = t.yToScreen(((Human) se).getY());
				typeColor = Color.PINK;
				flag = "FB";
				break;
			case POLICE_FORCE:
				ScreenBaseX = t.xToScreen(((Human) se).getX());
				ScreenBaseY = t.yToScreen(((Human) se).getY());
				typeColor = Color.cyan;
				flag = "PF";
				break;
			case CIVILIAN:
				ScreenBaseX = t.xToScreen(((Human) se).getX());
				ScreenBaseY = t.yToScreen(((Human) se).getY());
				typeColor = Color.green;
				flag = "Civilian";
				break;
			case BUILDING:
				ScreenBaseX = t.xToScreen(((Building) se).getX());
				ScreenBaseY = t.yToScreen(((Building) se).getY());
				flag = "Building";
				break;
			case ROAD:
				ScreenBaseX = t.xToScreen(((Road) se).getX());
				ScreenBaseY = t.yToScreen(((Road) se).getY());
				flag = "Road";
				break;
			case BLOCKADE:
				ScreenBaseX = t.xToScreen(((Blockade) se).getX());
				ScreenBaseY = t.yToScreen(((Blockade) se).getY());
				flag = "Block";
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		int round = 10, dist = 10, dist2 = 20, space = 5, height = g
				.getFontMetrics().getAscent();
		int[] xp = { ScreenBaseX + dist, ScreenBaseX + dist2,
				ScreenBaseX + dist2 };
		int[] yp = { ScreenBaseY, ScreenBaseY - space, ScreenBaseY + space };
		g.setColor(typeColor);
		g.fillPolygon(xp, yp, 3);
		g.setColor(Color.white);
		g.setStroke(new BasicStroke(2));
		g.drawPolygon(xp, yp, 3);
		int stringWidth = g.getFontMetrics().stringWidth(str) > g
				.getFontMetrics().stringWidth(
						flag + " " + se.getID().getValue()) ? g
				.getFontMetrics().stringWidth(str) : g.getFontMetrics()
				.stringWidth(flag + " " + se.getID().getValue());
		g.setColor(typeColor);
		g.setStroke(new BasicStroke(4));
		g.fillRoundRect(ScreenBaseX + round + dist, ScreenBaseY - height / 2,
				stringWidth + 20, g.getFontMetrics().getHeight() + height / 2,
				10, 10);
		g.setColor(Color.WHITE);
		g.drawRoundRect(ScreenBaseX + round + dist, ScreenBaseY - height / 2,
				stringWidth + 20, g.getFontMetrics().getHeight() + height / 2,
				10, 10);
		g.setColor(Color.BLACK);
		g.drawString(flag + " " + se.getID().getValue(), ScreenBaseX + round
				+ dist + 10, ScreenBaseY + height / 2);
		g.drawString(str, ScreenBaseX + round + dist + 10, ScreenBaseY + height
				+ g.getFontMetrics().getLeading() * 3);
		g.setStroke(new BasicStroke(1));
	}
}
