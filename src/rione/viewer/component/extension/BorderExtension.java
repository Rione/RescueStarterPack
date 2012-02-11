package rione.viewer.component.extension;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import rescuecore2.misc.gui.ScreenTransform;

/**
 * 縁を描画する装飾
 * @author utisam
 *
 */
public class BorderExtension implements EntityExtension {

	final private EntityExtension base;
	final private Color color;
	final private Stroke stroke;

	public static final BorderExtension BLACK = new BorderExtension(Color.BLACK);
	public static final BorderExtension BLUE = new BorderExtension(Color.BLUE);
	public static final BorderExtension CYAN = new BorderExtension(Color.CYAN);
	public static final BorderExtension DARK_GRAY = new BorderExtension(
			Color.DARK_GRAY);
	public static final BorderExtension GRAY = new BorderExtension(Color.GRAY);
	public static final BorderExtension GREEN = new BorderExtension(Color.GREEN);
	public static final BorderExtension LIGHT_GRAY = new BorderExtension(
			Color.LIGHT_GRAY);
	public static final BorderExtension MAGENTA = new BorderExtension(
			Color.MAGENTA);
	public static final BorderExtension ORANGE = new BorderExtension(
			Color.ORANGE);
	public static final BorderExtension PINK = new BorderExtension(Color.PINK);
	public static final BorderExtension RED = new BorderExtension(Color.RED);
	public static final BorderExtension WHITE = new BorderExtension(Color.WHITE);
	public static final BorderExtension YELLOW = new BorderExtension(
			Color.YELLOW);

	/**
	 * 縁を追加するExtensionです．
	 * 
	 * @param c
	 */
	public BorderExtension(Color c) {
		this(c, 1.0f);
	}

	/**
	 * 縁を追加するExtensionです．
	 * 
	 * @param c
	 * @param t
	 */
	public BorderExtension(Color c, double t) {
		this(c, (float) t);
	}

	/**
	 * 縁を追加するExtensionです．
	 * 
	 * @param c
	 * @param t
	 *            太さ
	 */
	public BorderExtension(Color c, float t) {
		this(c, new BasicStroke(t));
	}

	/**
	 * 縁を追加するExtensionです．
	 * 
	 * @param c
	 * @param s
	 */
	public BorderExtension(Color c, Stroke s) {
		this(null, c, s);
	}

	/**
	 * baseに加えて縁を追加します． 太さは1.0です．
	 * 
	 * @param base
	 * @param c
	 */
	public BorderExtension(EntityExtension base, Color c) {
		this(base, c, 1.0f);
	}

	/**
	 * baseに加えて縁を追加します．
	 * 
	 * @param base
	 * @param c
	 * @param t
	 *            太さ
	 */
	public BorderExtension(EntityExtension base, Color c, double t) {
		this(base, c, (float) t);
	}

	/**
	 * baseに加えて縁を追加します．
	 * 
	 * @param base
	 * @param c
	 * @param t
	 *            太さ
	 */
	public BorderExtension(EntityExtension base, Color c, float t) {
		this(base, c, new BasicStroke(t));
	}

	/**
	 * baseに加えて縁を追加します．
	 * 
	 * @param base
	 * @param c
	 * @param s
	 */
	public BorderExtension(EntityExtension base, Color c, Stroke s) {
		this.base = base;
		this.color = c;
		this.stroke = s;
	}

	/**
	 * このインスタンスをテンプレートとする生成関数を返します．
	 * 
	 * @return このインスタンスをテンプレートとする生成関数．
	 */
	public DecoratorFunction decorator() {
		return new DecoratorFunction() {
			@Override
			public EntityExtension create(EntityExtension base) {
				return new BorderExtension(base, color, stroke);
			}
		};
	}

	@Override
	public int render(Graphics2D g, ScreenTransform t, Shape s, int x, int y) {
		int yOffset = 0;
		if (base != null) {
			yOffset = base.render(g, t, s, x, y);
		}
		g.setColor(color);
		g.setStroke(stroke);
		g.draw(s);
		return yOffset;
	}

}
