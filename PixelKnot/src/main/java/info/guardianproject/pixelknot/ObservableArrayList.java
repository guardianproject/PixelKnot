package info.guardianproject.pixelknot;

import java.util.ArrayList;
import java.util.Collection;

public class ObservableArrayList<T> extends ArrayList<T> {

    public interface OnChangeListener {
        void onListChanged(ObservableArrayList list);
    }

    private OnChangeListener mOnChangeListener;

    public ObservableArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public ObservableArrayList(Collection<? extends T> c) {
        super(c);
    }

    public ObservableArrayList() {
        super();
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mOnChangeListener = listener;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        if (ret && mOnChangeListener != null)
            mOnChangeListener.onListChanged(this);
        return ret;
    }

    @Override
    public T remove(int index) {
        T ret = super.remove(index);
        if (ret != null && mOnChangeListener != null)
            mOnChangeListener.onListChanged(this);
        return ret;
    }

    @Override
    public boolean add(T t) {
        boolean ret = super.add(t);
        if (ret && mOnChangeListener != null)
            mOnChangeListener.onListChanged(this);
        return ret;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        if (mOnChangeListener != null)
            mOnChangeListener.onListChanged(this);
    }
}
