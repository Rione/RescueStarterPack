package rione.viewer.component.extension;

/**
 * 既存のExtensionにExtensionを付与するための関数オブジェクトのためのインタフェースです．
 * @see rione.viewer.component.extension.ExtensionMap
 * @author utisam
 *
 */
public interface DecoratorFunction {
	/**
	 * このインスタンスをテンプレートとしてbaseに装飾を付与します．
	 * @see rione.viewer.component.extension.ExtensionMap#putExtensionToAll(java.util.Collection, DecoratorFunction)
	 * @see rione.viewer.component.extension.ExtensionMap#putExtensionToAllID(java.util.Collection, DecoratorFunction)
	 * @param base
	 * @return baseに新しくExtensionを付与したもの
	 */
	EntityExtension create(EntityExtension base);
}
