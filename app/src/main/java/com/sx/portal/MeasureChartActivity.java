package com.sx.portal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.sx.portal.entity.MeasureEntity;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.util.Constants;
import com.sx.portal.util.DateTimeUtil;
import com.sx.portal.util.LevelUtil;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * author:Javier
 * time:2016/6/7.
 * mail:38244704@qq.com
 */
public class MeasureChartActivity extends SimpleActivity {

    private LineChartView mChart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 1;
    private int numberOfPoints = 7;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor;
    private boolean chartZoomEnable = false;

    private long startTime;
    private long endTime;

    private MemberEntity mCurrentMember = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_measure_chart);
    }

    @Override
    public void onInitView() {
        mChart = (LineChartView) findViewById(R.id.measure_chart);
        mChart.setZoomEnabled(chartZoomEnable);
    }

    @Override
    public void onSetEventListener() {

    }

    @Override
    public void onInitComplete() {
        mCurrentMember = getIntent().getParcelableExtra("member");

        startTime = DateTimeUtil.getTimesmorning().getTime();
        endTime = DateTimeUtil.getTimesnight().getTime();

        // Disable viewport recalculations, see toggleCubic() method for more info.
        mChart.setViewportCalculationEnabled(false);
        resetViewport();
    }

    @Override
    public void onRegister() {
        super.onRegister();

        Bundle bundle = new Bundle();
        bundle.putLong("startTime", startTime);
        bundle.putLong("endTime", endTime);
        bundle.putInt("memberId", mCurrentMember.getId());
        bundle.putString("order", "asc");
        notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_MEASURE_RECODE, bundle);
    }

    @Override
    public void onRemove() {
        super.onRemove();
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_READ_MEASURE_RECORD_RETURN
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_READ_MEASURE_RECORD_RETURN:
                // Generate some random values.
                generateValues(message.getList());
                generateData();
                break;
        }
    }

    private void generateValues(ArrayList<MeasureEntity> measureEntities) {
//        for (int i = 0; i < maxNumberOfLines; ++i) {
//            for (int j = 0; j < numberOfPoints; ++j) {
//                randomNumbersTab[i][j] = (float) Math.random() * 100f;
//            }
//        }

        numberOfPoints = measureEntities.size();

        for (int i = 0; i < measureEntities.size(); i++) {
            MeasureEntity entity = measureEntities.get(i);
            randomNumbersTab[0][i] = LevelUtil.pressure_chart_mark(entity.getSbp(), entity.getDbp());
        }

//        randomNumbersTab[0][0] = LevelUtil.pressure_chart_mark(0, 0);
//        randomNumbersTab[0][1] = LevelUtil.pressure_chart_mark(100, 60);
//        randomNumbersTab[0][2] = LevelUtil.pressure_chart_mark(126, 80);
//        randomNumbersTab[0][3] = LevelUtil.pressure_chart_mark(138, 92);
//        randomNumbersTab[0][4] = LevelUtil.pressure_chart_mark(142, 78);
//        randomNumbersTab[0][5] = LevelUtil.pressure_chart_mark(129, 120);
//        randomNumbersTab[0][6] = LevelUtil.pressure_chart_mark(189, 120);
    }

    private void generateData() {
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(true);
            line.setHasPoints(true);
            if (pointsHaveDifferentColor) {
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        ArrayList yList = new ArrayList();
        yList.add(0 * Constants.CHART_ITEM_RANGE);
        yList.add(1 * Constants.CHART_ITEM_RANGE);
        yList.add(2 * Constants.CHART_ITEM_RANGE);
        yList.add(3 * Constants.CHART_ITEM_RANGE);
        yList.add(4 * Constants.CHART_ITEM_RANGE);
        yList.add(5 * Constants.CHART_ITEM_RANGE);
        yList.add(100f);

        ArrayList yLable = new ArrayList();
        yLable.add(getString(R.string.level_1));
        yLable.add(getString(R.string.level_2));
        yLable.add(getString(R.string.level_3));
        yLable.add(getString(R.string.level_4));
        yLable.add(getString(R.string.level_5));
        yLable.add(getString(R.string.level_6));
        yLable.add("");

        Axis axisY = Axis.generateAxisFromCollection(yList, yLable);
        axisY.setHasLines(true);

        axisY.setName("  ");
        data.setAxisXBottom(null);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mChart.setLineChartData(data);
    }

    private void reset() {
        numberOfLines = 1;

        shape = ValueShape.CIRCLE;
        isFilled = false;
        hasLabels = false;
        pointsHaveDifferentColor = false;

        mChart.setValueSelectionEnabled(hasLabelForSelected);
        resetViewport();
    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(mChart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = numberOfPoints - 1;
        mChart.setMaximumViewport(v);
        mChart.setCurrentViewport(v);
    }
}