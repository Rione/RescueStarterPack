package rione.viewer.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

public class EntityExtension {
	
	/** 色 */
	final private Color color;
	/** 線のスタイル */
	final private Stroke stroke;
	/** 塗りつぶすか */
	final private boolean fill;

	public EntityExtension(Color c) {
		this(c, new BasicStroke(), false);
	}
	
	public EntityExtension(Color c, Stroke s) {
		this(c, s, false);
	}
	
	public EntityExtension(Color c, double thick) {
		this(c, new BasicStroke((float) thick), false);
	}
	
	public EntityExtension(Color c, boolean f) {
		this(c, new BasicStroke(), f);
	}
	
	public EntityExtension(Color c, float thick, boolean f) {
		this(c, new BasicStroke(thick), f);
	}
	
	public EntityExtension(Color c, Stroke s, boolean f) {
		color = c;
		stroke = s;
		fill = f;
	}

	public Color getColor() {
		return color;
	}
	public Stroke getStroke() {
		return stroke;
	}
	public boolean getFill() {
		return fill;
	}
}
