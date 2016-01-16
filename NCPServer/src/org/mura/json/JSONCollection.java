package org.mura.json;

/**
 * JSONCollection: JSON容器接口
 * <p>
 * 可以包含其它JSON变量的JSON变量, 可以实现此接口, 用以添加其它的JSON变量
 * 
 * @author mura
 *
 */
public interface JSONCollection {

	/**
	 * 添加一个JSON变量, 根据具体容器的不同, 效果可能有所不同
	 * 
	 * @param var
	 *            变量
	 */
	@SuppressWarnings("rawtypes")
	public void add(JSONVariable var);

	/**
	 * 获取容器当前的子项个数
	 * 
	 * @return 个数
	 */
	public int size();

	/**
	 * 清空当前容器
	 */
	public void clear();

}