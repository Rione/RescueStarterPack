package rione.viewer.component.extension;

import java.awt.Graphics2D;
import java.awt.Shape;

import rescuecore2.misc.gui.ScreenTransform;

public interface EntityExtension {
	/**
	 * gに対してsの描画を行う際に呼び出されます．
	 * customExtension内でExtensionMapに追加してください．
	 * @see rione.viewer.component.extension.ExtensionMap
	 * @param g 描画対象
	 * @param t 変換行列
	 * @param s 装飾対象
	 * @param x 装飾対象エンティティのx
	 * @param y 装飾対象エンティティのy
	 * @return テキストのyOffset
	 */
	int render(Graphics2D g, ScreenTransform t, Shape s, int x, int y);
}
