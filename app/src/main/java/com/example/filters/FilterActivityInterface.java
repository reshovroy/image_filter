package com.example.filters;

import android.graphics.Bitmap;

public interface FilterActivityInterface {

    public void filterAppliedCallback(Bitmap result_bitmap);
    public void filterListItemSelectedCallback(MatrixFilter filter);
}
