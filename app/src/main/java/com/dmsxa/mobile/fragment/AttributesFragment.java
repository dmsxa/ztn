

package com.dmsxa.mobile.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.keenfin.easypicker.PhotoPicker;
import com.dmsxa.maplib.api.IGISApplication;
import com.dmsxa.maplib.datasource.Field;
import com.dmsxa.maplib.datasource.GeoGeometryFactory;
import com.dmsxa.maplib.datasource.GeoLineString;
import com.dmsxa.maplib.datasource.GeoMultiLineString;
import com.dmsxa.maplib.datasource.GeoMultiPoint;
import com.dmsxa.maplib.datasource.GeoMultiPolygon;
import com.dmsxa.maplib.datasource.GeoPoint;
import com.dmsxa.maplib.datasource.GeoPolygon;
import com.dmsxa.maplib.map.VectorLayer;
import com.dmsxa.maplib.util.Constants;
import com.dmsxa.maplib.util.GeoConstants;
import com.dmsxa.maplib.util.LocationUtil;
import com.dmsxa.maplibui.GISApplication;
import com.dmsxa.maplibui.api.IVectorLayerUI;
import com.dmsxa.maplibui.control.PhotoGallery;
import com.dmsxa.maplibui.fragment.BottomToolbar;
import com.dmsxa.maplibui.overlay.EditLayerOverlay;
import com.dmsxa.maplibui.util.ControlHelper;
import com.dmsxa.maplibui.util.SettingsConstantsUI;
import com.dmsxa.mobile.R;
import com.dmsxa.mobile.activity.MainActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dmsxa.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.dmsxa.maplib.util.GeoConstants.CRS_WGS84;
import static com.dmsxa.maplib.util.GeoConstants.FTDate;
import static com.dmsxa.maplib.util.GeoConstants.FTDateTime;
import static com.dmsxa.maplib.util.GeoConstants.FTTime;
import static com.dmsxa.maplib.util.GeoConstants.GTLineString;
import static com.dmsxa.maplib.util.GeoConstants.GTMultiLineString;
import static com.dmsxa.maplib.util.GeoConstants.GTMultiPoint;
import static com.dmsxa.maplib.util.GeoConstants.GTMultiPolygon;
import static com.dmsxa.maplib.util.GeoConstants.GTPoint;
import static com.dmsxa.maplib.util.GeoConstants.GTPolygon;
import static com.dmsxa.maplib.util.NetworkUtil.URL_PATTERN;
import static com.dmsxa.mobile.util.AppConstants.DEFAULT_COORDINATES_FRACTION_DIGITS;

public class AttributesFragment
        extends Fragment
{
    protected static final String KEY_ITEM_ID       = "item_id";
    protected static final String KEY_ITEM_POSITION = "item_pos";

    private LinearLayout    mAttributes;
    private VectorLayer     mLayer;
    private List<Long>      mFeatureIDs;

    private long        mItemId;
    private int         mItemPosition;
    private boolean     mIsTablet;

    protected EditLayerOverlay mEditLayerOverlay;
    protected Menu mBottomMenu;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        if (mLayer == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            Toast.makeText(getContext(), R.string.error_layer_not_inited, Toast.LENGTH_SHORT).show();
            return null;
        }

        getActivity().setTitle(mLayer.getName());
        setHasOptionsMenu(!isTablet());

        int resId = isTablet() ? R.layout.fragment_attributes_tab : R.layout.fragment_attributes;
        View view = inflater.inflate(resId, container, false);

        if (isTablet()) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(view.getLayoutParams());
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            lp.width = metrics.widthPixels / 2;

            int[] attrs = {R.attr.actionBarSize};
            TypedArray ta = getActivity().obtainStyledAttributes(attrs);
            lp.bottomMargin = ta.getDimensionPixelSize(0, 0);
            ta.recycle();

            view.setLayoutParams(lp);
        }

        mAttributes = view.findViewById(R.id.ll_attributes);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setAttributes();
        ((MainActivity) getActivity()).setActionBarState(isTablet());
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() == R.id.menu_about || item.getItemId() == R.id.menu_settings) {
                continue;
            }
            item.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onDestroyView()
    {
        ((MainActivity) getActivity()).restoreBottomBar(MapFragment.MODE_SELECT_ACTION);
        super.onDestroyView();
    }


    public void setSelectedFeature(
            VectorLayer selectedLayer,
            long selectedItemId)
    {
        mItemId = selectedItemId;
        mLayer = selectedLayer;

        if (mLayer == null)
            return;

        mFeatureIDs = mLayer.query(null); // get all feature IDs

        for (int i = 0; i < mFeatureIDs.size(); i++) {
            if (mFeatureIDs.get(i) == mItemId) {
                mItemPosition = i;
                break;
            }
        }

        setAttributes();
    }


    private void setAttributes()
    {
        if (mAttributes == null)
            return;

        mAttributes.removeAllViews();

        int[] attrs = new int[] {android.R.attr.textColorPrimary};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        String textColor = Integer.toHexString(ta.getColor(0, Color.BLACK)).substring(2);
        ta.recycle();

        final WebView webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        String data = "<!DOCTYPE html><html><head><meta charset='utf-8'><style>body{word-wrap:break-word;color:#" + textColor + ";font-family:Roboto Light,sans-serif;font-weight:300;line-height:1.15em}.flat-table{table-layout:fixed;margin-bottom:20px;width:100%;border-collapse:collapse;border:none;box-shadow:inset 1px -1px #ccc,inset -1px 1px #ccc}.flat-table td{box-shadow:inset -1px -1px #ccc,inset -1px -1px #ccc;padding:.5em}.flat-table tr{-webkit-transition:background .3s,box-shadow .3s;-moz-transition:background .3s,box-shadow .3s;transition:background .3s,box-shadow .3s}</style></head><body><table class='flat-table'><tbody>";

        FragmentActivity activity = getActivity();
        if (null == activity)
            return;

        ((MainActivity) activity).setSubtitle(String.format(getString(R.string.features_count_attributes), mItemPosition + 1, mFeatureIDs.size()));
        checkNearbyItems();

        try {
            data = parseAttributes(data);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        data += "</tbody></table></body></html>";
        webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
        mAttributes.addView(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.setBackgroundColor(Color.TRANSPARENT);
            }
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });

        IGISApplication app = (GISApplication) getActivity().getApplication();
        final Map<String, Integer> mAttaches = new HashMap<>();
        PhotoGallery.getAttaches(app.getAuthority(), mLayer, mItemId, mAttaches, false, null);

        if (mAttaches.size() > 0) {
            final PhotoPicker gallery = new PhotoPicker(getActivity(), true);
            int px = ControlHelper.dpToPx(16, getResources());
            gallery.setDefaultPreview(true);
            gallery.setPadding(px, 0, px, 0);
            gallery.post(new Runnable() {
                @Override
                public void run() {
                    gallery.restoreImages(new ArrayList<>(mAttaches.keySet()));
                }
            });

            mAttributes.addView(gallery);
        }
    }

    private String parseAttributes(String data) throws RuntimeException {
        String selection = Constants.FIELD_ID + " = ?";
        Cursor attributes = mLayer.query(null, selection, new String[]{mItemId + ""}, null, null);
        if (null == attributes || attributes.getCount() == 0)
            return data;

        if (attributes.moveToFirst()) {
            StringBuilder dataBuilder = new StringBuilder(data);
            for (int i = 0; i < attributes.getColumnCount(); i++) {
                String column = attributes.getColumnName(i);
                String text, alias;

                if (column.startsWith(Constants.FIELD_GEOM_))
                    continue;

                if (column.equals(Constants.FIELD_GEOM)) {
                    switch (mLayer.getGeometryType()) {
                        case GTPoint:
                            try {
                                GeoPoint pt = (GeoPoint) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.coordinates), formatCoordinates(pt)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        case GTMultiPoint:
                            try {
                                GeoMultiPoint mpt = (GeoMultiPoint) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.center), formatCoordinates(mpt.getEnvelope().getCenter())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        case GTLineString:
                            try {
                                GeoLineString line = (GeoLineString) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.length), LocationUtil.formatLength(getContext(), line.getLength(), 3)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        case GTMultiLineString:
                            try {
                                GeoMultiLineString multiline = (GeoMultiLineString) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.length), LocationUtil.formatLength(getContext(), multiline.getLength(), 3)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        case GTPolygon:
                            try {
                                GeoPolygon polygon = (GeoPolygon) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.perimeter), LocationUtil.formatLength(getContext(), polygon.getPerimeter(), 3)));
                                dataBuilder.append(getRow(getString(R.string.area), LocationUtil.formatArea(getContext(), polygon.getArea())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        case GTMultiPolygon:
                            try {
                                GeoMultiPolygon polygon = (GeoMultiPolygon) GeoGeometryFactory.fromBlob(attributes.getBlob(i));
                                dataBuilder.append(getRow(getString(R.string.perimeter), LocationUtil.formatLength(getContext(), polygon.getPerimeter(), 3)));
                                dataBuilder.append(getRow(getString(R.string.area), LocationUtil.formatArea(getContext(), polygon.getArea())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        default:
                            continue;
                    }
                }

                Field field = mLayer.getFieldByName(column);
                int fieldType = field != null ? field.getType() : Constants.NOT_FOUND;
                switch (fieldType) {
                    case GeoConstants.FTInteger:
                        text = attributes.getInt(i) + "";
                        break;
                    case GeoConstants.FTReal:
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumFractionDigits(4);
                        nf.setGroupingUsed(false);
                        Double value = attributes.getDouble(i);
                        if (value.isNaN())
                            continue;
                        text = nf.format(value);
                        break;
                    case GeoConstants.FTDate:
                    case GeoConstants.FTTime:
                    case GeoConstants.FTDateTime:
                        text = formatDateTime(attributes.getLong(i), fieldType);
                        break;
                    default:
                        text = toString(attributes.getString(i));
                        Pattern pattern = Pattern.compile(URL_PATTERN);
                        Matcher match = pattern.matcher(text);
                        while (match.matches()) {
                            String url = text.substring(match.start(), match.end());
                            text = text.replaceFirst(URL_PATTERN, "<a href = '" + url + "'>" + url + "</a>");
                            match = pattern.matcher(text.substring(match.start() + url.length() * 2 + 17));
                        }
                        break;
                }

                if (field != null)
                    alias = field.getAlias();
                else if (column.equals(Constants.FIELD_ID))
                    alias = Constants.FIELD_ID;
                else
                    alias = "";

                dataBuilder.append(getRow(alias, text));
            }
            data = dataBuilder.toString();
        }

        attributes.close();
        return data;
    }


    protected String getRow(String column, String text) {
        column = column == null ? "" : toString(column);
        text = text == null ? "" : text;
        return String.format("<tr><td>%s</td><td>%s</td></tr><tr>", column, text);
    }


    protected String toString(String text) {
        return text == null ? "" : Html.fromHtml(text).toString();
    }


    protected String formatCoordinates(GeoPoint pt) {
        pt.setCRS(CRS_WEB_MERCATOR);
        pt.project(CRS_WGS84);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int format = Integer.parseInt(prefs.getString(SettingsConstantsUI.KEY_PREF_COORD_FORMAT, Location.FORMAT_DEGREES + ""));
        int fraction = prefs.getInt(SettingsConstantsUI.KEY_PREF_COORD_FRACTION, DEFAULT_COORDINATES_FRACTION_DIGITS);

        String lat = getString(R.string.latitude_caption_short) + ": " +
                LocationUtil.formatLatitude(pt.getY(), format, fraction, getResources());
        String lon = getString(R.string.longitude_caption_short) + ": " +
                LocationUtil.formatLongitude(pt.getX(), format, fraction, getResources());

        return lat + "<br \\>" + lon;
    }


    public static String formatDateTime(long millis, int type) {
        String result = millis + "";
        SimpleDateFormat sdf = null;

        switch (type) {
            case FTDate:
                sdf = (SimpleDateFormat) DateFormat.getDateInstance();
                break;
            case FTTime:
                sdf = (SimpleDateFormat) DateFormat.getTimeInstance();
                break;
            case FTDateTime:
                sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                break;
        }

        if (sdf != null)
            try {
                result = sdf.format(new Date(millis));
            } catch (Exception e) {
                e.printStackTrace();
            }

        return result;
    }


    private void checkNearbyItems() {
        boolean hasNext = mItemPosition + 1 <= mFeatureIDs.size() - 1;
        boolean hasPrevious = mItemPosition - 1 >= 0;

        if (mBottomMenu != null) {
            ControlHelper.setEnabled(mBottomMenu.findItem(R.id.menu_prev), hasPrevious);
            ControlHelper.setEnabled(mBottomMenu.findItem(R.id.menu_next), hasNext);
        }
    }


    public void selectItem(boolean isNext)
    {
        boolean hasItem = false;

        if (isNext) {
            if (mItemPosition < mFeatureIDs.size() - 1) {
                mItemPosition++;
                hasItem = true;
            }
        } else {
            if (mItemPosition > 0) {
                mItemPosition--;
                hasItem = true;
            }
        }

        if (hasItem) {
            mItemId = mFeatureIDs.get(mItemPosition);
            setAttributes();
            if (null != mEditLayerOverlay) {
                mEditLayerOverlay.setSelectedFeature(mItemId);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_ITEM_ID, mItemId);
        outState.putInt(KEY_ITEM_POSITION, mItemPosition);
    }


    @Override
    public void onViewStateRestored(
            @Nullable
            Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mItemId = savedInstanceState.getLong(KEY_ITEM_ID);
            mItemPosition = savedInstanceState.getInt(KEY_ITEM_POSITION);
        }
    }


    public void setTablet(boolean tablet)
    {
        mIsTablet = tablet;
    }


    public boolean isTablet()
    {
        return mIsTablet;
    }


    public void setToolbar(
            final BottomToolbar toolbar,
            EditLayerOverlay overlay)
    {
        if (null == mLayer)
            return;

        mEditLayerOverlay = overlay;

        if (!isTablet())
            toolbar.getBackground().setAlpha(255);

        mBottomMenu = toolbar.getMenu();
        if (mBottomMenu != null)
            mBottomMenu.clear();

        toolbar.inflateMenu(R.menu.attributes);
        toolbar.setOnMenuItemClickListener(
                new BottomToolbar.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        if (null == mLayer) {
                            return false;
                        }
                        if (menuItem.getItemId() == R.id.menu_next) {
                            selectItem(true);
                            return true;
                        } else if (menuItem.getItemId() == R.id.menu_prev) {
                            selectItem(false);
                            return true;
                        } else if (menuItem.getItemId() == R.id.menu_edit_attributes) {
                            IVectorLayerUI vectorLayerUI = (IVectorLayerUI) mLayer;
                            if (null != vectorLayerUI)
                                vectorLayerUI.showEditForm(getActivity(), mItemId, null);
                            return true;
                        }

                        return true;
                    }
                });

        checkNearbyItems();
    }
}