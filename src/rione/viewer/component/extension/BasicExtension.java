package rione.viewer.component.extension;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import rescuecore2.misc.gui.ScreenTransform;

/**
 * 基本装飾<br>
 * @author utisam
 * @deprecated あまり便利ではない．
 * 他のExtensionを組み合わせることに慣れたほうがよい．
 */
@Deprecated
public class BasicExtension implements EntityExtension {

	/** 色 */
	final private Color color;
	/** 線のスタイル */
	final private Stroke stroke;
	/** 塗りつぶすか */
	final private boolean fill;

	public BasicExtension(Color c) {
		this(c, new BasicStroke(), false);
	}
	
	public BasicExtension(Color c, Stroke s) {
		this(c, s, false);
	}
	
	public BasicExtension(Color c, double thick) {
		this(c, new BasicStroke((float) thick), false);
	}
	
	public BasicExtension(Color c, boolean f) {
		this(c, new BasicStroke(), f);
	}
	
	public BasicExtension(Color c, float thick, boolean f) {
		this(c, new BasicStroke(thick), f);
	}
	
	public BasicExtension(Color c, Stroke s, boolean f) {
		color = c;
		stroke = s;
		fill = f;
	}

	@Override
	public int render(Graphics2D g, ScreenTransform t,Shape s,int x,int y) {
		g.setColor(color);
		g.setStroke(stroke);
		if (fill) {
			g.fill(s);
		} else {
			g.draw(s);
		}
		return 0;
	}
}
