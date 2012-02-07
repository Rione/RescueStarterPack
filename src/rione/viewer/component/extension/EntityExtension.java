package rione.viewer.component.extension;

import java.awt.Graphics2D;
import java.awt.Shape;

public interface EntityExtension {
	/**
	 * gに対してsの描画を行う際に呼び出されます．
	 * @param g
	 * @param s
	 * @return テキストのyOffset．
	 */
	int render(Graphics2D g, Shape s);
}
