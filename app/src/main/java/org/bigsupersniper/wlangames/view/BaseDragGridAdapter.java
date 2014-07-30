package org.bigsupersniper.wlangames.view;

/**
 * Created by linfeng on 2014/7/31.
 */
public interface BaseDragGridAdapter {
    /**
     * 重新排列数据
     * @param oldPosition
     * @param newPosition
     */
    public void reorderItems(int oldPosition, int newPosition);


    /**
     * 设置某个item隐藏
     * @param hidePosition
     */
    public void setHideItem(int hidePosition);
}
