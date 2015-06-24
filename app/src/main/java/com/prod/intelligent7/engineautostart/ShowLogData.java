package com.prod.intelligent7.engineautostart;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by eric on 2015/6/8.
 */
public class ShowLogData extends MySimpleFragment {


    public static HashMap<String, String> mcuDictionary=null;

    private static void buildCodeDictionary() {
        if (mcuDictionary!=null) return;
        mcuDictionary=new HashMap<String, String>();

        mcuDictionary.put("DAEMON GONE","通信系统结束");

        mcuDictionary.put("M1-00","冷气启动");
        mcuDictionary.put("M1-01","暖气启动");
        mcuDictionary.put("M2","车载机密码更换");
        mcuDictionary.put("M3","手机號码设定");
        mcuDictionary.put("M4-00","立即关闭引擎");
        mcuDictionary.put("M4-01","立即关闭冷气");
        mcuDictionary.put("M5","立即启动");
        //mcuDictionary.put("M5","立即启动");

        mcuDictionary.put("S110", "暖气设定成功");

        mcuDictionary.put("S111", "暖气设定失败");

        mcuDictionary.put("S100", "冷氣設定成功");

        mcuDictionary.put("S101", "冷氣設定失敗");

        mcuDictionary.put("S200", "车载机密码设定成功");

        mcuDictionary.put("S201", "车载机密码设定失败");

        mcuDictionary.put("S300", "手机号码设定成功");

        mcuDictionary.put("S301", "手机号码设定失败");

        mcuDictionary.put("S400", "引擎已关闭");

        mcuDictionary.put("S401", "引擎由车主启动,不能关闭");

        mcuDictionary.put("S410", "冷氣已關閉");

        mcuDictionary.put("S411", "冷氣關閉失敗");

        mcuDictionary.put("S500", "引擎已启动");

        mcuDictionary.put("S501", "引擎启动失败");

        mcuDictionary.put("S502", "引擎启动成功");

        mcuDictionary.put("S503", "偷车");

        mcuDictionary.put("S504", "暖车失败");

        mcuDictionary.put("S505", "暖车完毕");

        mcuDictionary.put("S999", "手机号码未授权");
    }

    public static String translate(String inCode){
        if (mcuDictionary==null) buildCodeDictionary();
        return mcuDictionary.get(inCode);
    }
    Logger log;
    int recordCount;

    ArrayList< HashMap<String, String> > allRecords;
    String[] allMessages;

    int getRecordCount()
    {
        return 0;
    }
    public static final DecimalFormat dF=new DecimalFormat("00");
    public static final int SHOW_FAILED1=11;
    public static final int SHOW_FAILED10=101;
    public static final int SHOW_NEWEST=10;
    public static final int SHOW_LAST10=12;
    public static final int SHOW_ALL=100;
    void buildListArray(int for1)
    {
        String sql;
        switch (for1)
        {
            case SHOW_NEWEST:
                sql="SELECT * FROM event_records ORDER BY event_time DESC LIMIT 1;";
                break;
            case SHOW_FAILED1:
                sql="SELECT * FROM event_records WHERE i_o='I' and message LIKE 'S__1' ORDER BY event_time DESC LIMIT 1;";
                break;
            case SHOW_FAILED10:
                sql="SELECT * FROM event_records WHERE i_o='I' and message LIKE 'S__1' ORDER BY event_time DESC LIMIT 10;";
                break;
            case SHOW_LAST10:
                sql="SELECT * FROM event_records ORDER BY event_time DESC LIMIT 10;";
                break;
            case SHOW_ALL:
                sql="SELECT * FROM event_records ORDER BY event_time DESC LIMIT 100;";
                break;
            default:
                sql="SELECT * FROM event_records ORDER BY event_time DESC LIMIT "+for1+" ;";
                break;
        }
        try {
            getAllListRecord(DbProcessor.getRecordsFromSql(mContext, sql));
            //I also get filled table
        } catch (SQLiteException e){
            e.printStackTrace();
        }
        allMessages=new String[allRecords.size()];
        buildCodeDictionary();
        for (int i=0; i<allMessages.length; i++)
        {
            HashMap<String, String> aRow=allRecords.get(i);
            long eTime=Long.parseLong(aRow.get("event_time"));
            GregorianCalendar gDate=new GregorianCalendar(TimeZone.getTimeZone("Hongkong"));
            if (eTime > 0)
            gDate.setTimeInMillis(eTime);
            String sTime=gDate.get(Calendar.YEAR)+"/"+
                    dF.format(gDate.get(Calendar.MONTH) + 1)+"/"+
                    dF.format(gDate.get(Calendar.DAY_OF_MONTH))+" ";
            sTime += dF.format(gDate.get(Calendar.HOUR_OF_DAY))+":"+dF.format(gDate.get(Calendar.MINUTE));
            String msg=aRow.get("message");
            String chinese;
            String key=msg;
            String last4="";
            if (msg.charAt(0)=='M'){
                if (msg.charAt(1)=='2' || msg.charAt(1)=='3' || msg.charAt(1)=='5')
                    key=msg.substring(0,2);
                if (msg.charAt(1)=='5')
                    last4=msg.substring(3, 3+2)+mContext.getResources().getString(R.string.minute_long);
            }

            chinese=mcuDictionary.get(key)+last4;

            if (MCU_NAME==null)
                MCU_NAME=getSavedString(MainActivity.SET_SIM);
            String type=mContext.getResources().getString(R.string.send_to);//send);
            String sender=aRow.get("sender");
            String receiver=aRow.get("receiver");
            String i_o=aRow.get("i_o");
            if (i_o.charAt(0)=='I'){
                type=mContext.getResources().getString(R.string.got_from);//
            }
            type += (":"+MCU_NAME.substring(0, MCU_NAME.length()>12?12:MCU_NAME.length())+":");

            allMessages[i]=sTime+" "+type+chinese;
        }
        //use array for list view
    }

    public void clearLog(Context cx)
    {
        DbProcessor.execSQL(cx, "Delete from event_records;");
        //or DbProcessor.emptyTable(cx, "event_records");
    }

    public static String logTableName="event_records";

    PopupWindow showUp=null;
    public void showLog(Context cx, int for1){

        mContext=cx;
        buildListArray(for1);
        ViewGroup container=(ViewGroup)((MainActivity)mContext).findViewById(R.id.container);
        View getAnchor=((MainActivity)mContext).getLayoutInflater().inflate(R.layout.layout_for_pop, container, false);//null);
        TextView popTitle=(TextView)getAnchor.findViewById(R.id.pop_title);
        String title="";
        switch (for1){
            case SHOW_NEWEST:
                title=((MainActivity)mContext).getResources().getString(R.string.action_get_recent1);
                break;
            case SHOW_FAILED1:
                title=((MainActivity)mContext).getResources().getString(R.string.action_get_last_failed);
                break;
            case SHOW_FAILED10:
                title=((MainActivity)mContext).getResources().getString(R.string.action_get_failed10);
                break;
            case SHOW_LAST10:
                title=((MainActivity)mContext).getResources().getString(R.string.action_get_recent10);
                break;
            case SHOW_ALL:
                title=((MainActivity)mContext).getResources().getString(R.string.open_log);
                break;
            default:
                title=((MainActivity)mContext).getResources().getString(R.string.action_get_recent10);
                break;
        }
        popTitle.setText(title);
        Button dismiss=(Button)getAnchor.findViewById(R.id.pop_anchor);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showUp != null)
                    showUp.dismiss();
            }
        });
        ListView v=new ListView(mContext);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                    R.layout.layout_one_line_text, allMessages);
        v.setAdapter(adapter);
            int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
            v.setOnItemClickListener(null);
        v.setSelection(0);
        v.setBackgroundColor(0xffffffff);
        v.setLayoutParams(new LinearLayout.LayoutParams(mDisplayWidth*4/5, mDisplayHeight*3/4, 1));
        ((ViewGroup)getAnchor).addView(v);
        //PopupWindow showUp=new PopupWindow(v, mDisplayWidth*5/6, mDisplayHeight*4/5, true);

        //getAnchor=((MainActivity)mContext).findViewById(R.id.container);

        //showUp.showAtLocation(dismiss, Gravity.CENTER, 0, 0);
    }

    Vector<ContentValues> getAllListRecord(Cursor aCursor) //table will be filled too
    {
        if (aCursor==null) {
            DbProcessor.closeDbByCursor(aCursor);
            return null;
        }
        if (aCursor.getCount()<1)
        {
            DbProcessor.closeDbByCursor(aCursor);
            new MyToast(mContext, getResources().getString(R.string.no_record_to_show)).warn();
        }
        //Log.d("dbDATA", "CALLED BY" + this.getId());
        Vector<ContentValues> allRows=new Vector<ContentValues>();
        if (allRecords==null)
            allRecords=new ArrayList<HashMap<String, String>>();
        allRecords.clear();
        
        //firstNew=null;
        int selectedId=-1;
        int iShow=-1;
        String[] heads=aCursor.getColumnNames();
        aCursor.moveToFirst();
        while (!aCursor.isAfterLast())
        {
            ContentValues aC= new ContentValues();
            for (int k=0; k<heads.length; k++)
            {
                aC.put(heads[k],  aCursor.getString(k) );
                /*
                switch (aCursor.getType(k))
                {
                    case Cursor.FIELD_TYPE_NULL:
                        aC.put(heads[k], aCursor.getString(k) ); break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        aC.put(heads[k], ""+aCursor.getInt(k) ); break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        aC.put(heads[k], ""+aCursor.getFloat(k) ); break;
                    case Cursor.FIELD_TYPE_STRING:
                        aC.put(heads[k], aCursor.getString(k) ); break;
                    case Cursor.FIELD_TYPE_BLOB:
                        aC.put(heads[k], new String(aCursor.getBlob(k)) ); break;
                    default:
                        aC.put(heads[k],  aCursor.getString(k) );
                        break;
                }*/
            }
            String atTime=aC.getAsString("event_time");
            /*String tt=aC.getAsString("event_title");
            //String status=aC.getAsString(STATUS);
            //Log.d("dbDATA", dd+tt+aC.getAsString(STATUS));
            if (selectedValues != null && selectedId<0){
                if (dd.equalsIgnoreCase(selectedValues.get("event_date")) &&
                        tt.equalsIgnoreCase(selectedValues.get("event_title")))
                    selectedId=aCursor.getPosition();
            }
            */
            allRows.add(aC);
            allRecords.add(fromContentValuesToMap(aC));
            aCursor.moveToNext();
        }
        DbProcessor.closeDbByCursor(aCursor);

        return allRows;
    }

    Context mContext;
    protected DbProcessor mDbExcutor;
    protected SQLiteDatabase mDb;
    protected ContentValues mContentValues;
    protected String criteria;   // for rawQuery you can use ... where abc > ? and efg < ? and those ? can be replaced by
    protected String[] criteria_values;
    Vector<ContentValues> mAllRecords;
    //for Cursor
    protected Vector<String> listColumns;//={"rowid _id", "visit_date", "full_name"}; id is mandatory for simpleCursorAdapter
    protected Vector<String> showColumns;//={"visit_date", "full_name"};
    String mTableName;

    String createTableSql()
    {
        String sql="create table if not exists event_records ";
        sql += "( event_time numeric not null,";
        //sql += " message text unique not null,";
        sql += " message text,";
        sql += " i_o char(1),";
        sql += " sender char(20),";
        sql += " receiver char(20));";
        //sql += " event_description text,";
        //sql += " status char(8) not null default 'new',";
        //sql += " participants char(8) default '0' );";//new, cancle, read, join
        return sql;
    }


    void getAllRecords()
    {
        String sql="select * from event_records order by event_time desc";
        mAllRecords=getAllListRecord(DbProcessor.getRecordsFromSql(mContext, sql));
    }


    void initDB()
    {
        //setConstants();
        mDbExcutor=null;
        startDb();
        mTableName="event_records";

        if (!DbProcessor.ifTableExists(mContext, mTableName))
        {
            DbProcessor.createTable(mContext, createTableSql());
            //checkForServerData();
        }
        //else getAllRecords();
    }
    public void startDb()
    {
        if (mContext==null) {
            log.warning("Context is not set! cannot open database");
            return;
        }
        try {
            if (mDbExcutor == null || !mDbExcutor.getDb().isOpen() )
                mDbExcutor=new DbProcessor(mContext, "engine_auto_db");
            mDb=mDbExcutor.getDb();
        } catch (SQLiteException e){}

        //{Toast.makeText(getActivity(), e.getMessage()+" CAll 0986056745", Toast.LENGTH_LONG).show();}

    }

    void confirmTableExist(String table)
    {
        if (!DbProcessor.ifTableExists(mContext, mTableName))
        {
            DbProcessor.createTable(mContext, createTableSql());
            //checkForServerData();
        }
    }
    void doInsert(String tableName, ContentValues aRow)
    {
        //startDb();
        confirmTableExist(tableName);
        String eMsg="";
        try {
            DbProcessor.insertTable(mContext, tableName, null, aRow);//mContentValues);
        } catch (SQLiteConstraintException e){ log.warning(e.getMessage());}
        catch (SQLiteException e){ log.warning(e.getMessage());}
        //if (eMsg.length() >0)
        //{Toast.makeText(mActivity, eMsg, Toast.LENGTH_SHORT).show();}
    }

    void saveDataToDb(String data, String sender, String receiver, String io){
        confirmTableExist("event_records");
        String sql="insert into event_records ";
        long when=new Date().getTime();
        String message=data;
        int i0x=data.indexOf("@");
        if (i0x > 0){
            when = Long.parseLong(data.substring(i0x+1));
            message=data.substring(0, i0x);
        }
        sql += ("(event_time, message, sender, receiver, i_o) values ("+when+", '"+
                message+"', '"+sender+"', '"+receiver+"', '"+io+"' );"	);
        try {
            DbProcessor.insertTable(mContext, sql);
        }catch (SQLiteException e){
            log.warning("failed to insert data "+data+" for "+e.getMessage());
        }
    }

    void saveDataToDb(String data)
    {
       // saveDataToDb(data,myName, mySimIccId, "O");
    }

    String fromSpecifedFieldsToSqlCriteria(HashMap<String, String> specifiedFields)
    {
        if (specifiedFields==null || specifiedFields.size()<1) return "";
        String criteria=" where ";
        Set<String> keys=specifiedFields.keySet();
        Iterator<String> itr=keys.iterator();
        boolean once=true;
        while (itr.hasNext())
        {
            if (!once){ criteria += " and ";} else once=false;
            String key= itr.next();
            criteria += key;
            criteria += "='";
            criteria += specifiedFields.get(key);
            criteria += "'";
        }
        return criteria;
    }

    HashMap<String, String> fromContentValuesToMap(ContentValues aC)
    {
        if (aC==null || aC.size()<1) return null;
        HashMap<String, String> aMap=new HashMap<String, String>();
        Set<String> keys=aC.keySet();
        Iterator<String> itr=keys.iterator();
        while (itr.hasNext())
        {
            String key= itr.next();
            aMap.put(key, aC.getAsString(key));
        }
        return aMap;
    }
    ContentValues fromMapToContentValues(HashMap<String, String> aC)
    {
        if (aC==null || aC.size()<1) return null;
        ContentValues oC=new ContentValues();
        Set<String> keys=aC.keySet();
        Iterator<String> itr=keys.iterator();
        while (itr.hasNext())
        {
            String key= itr.next();
            if (key.compareToIgnoreCase("table_name")==0) continue;
            oC.put(key, aC.get(key));
        }
        return oC;
    }

    HashMap<String, String> selectedValues;

    Vector<ContentValues> getAllListRecord1(Cursor aCursor)
    {
        if (aCursor.getCount() <1)
        {
            aCursor.close();
            //Toast.makeText(getContext(), "NO RECORD TO SHOW", Toast.LENGTH_LONG).show();
        }
        //log.info("dbDATA" +"CALLED BY"+this.getId());
        Vector<ContentValues> allRows=new Vector<ContentValues>();
        //firstNew=null;
        int selectedId=-1;
        int iNew=-1;
        String[] heads=aCursor.getColumnNames();
        aCursor.moveToFirst();
        while (!aCursor.isAfterLast())
        {
            ContentValues aC= new ContentValues();
            for (int k=0; k<aCursor.getColumnCount(); k++)
            {
                switch (aCursor.getType(k))
                {
                    case Cursor.FIELD_TYPE_NULL:
                        aC.put(heads[k], "0"); break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        aC.put(heads[k], ""+aCursor.getInt(k) ); break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        aC.put(heads[k], ""+aCursor.getFloat(k) ); break;
                    case Cursor.FIELD_TYPE_STRING:
                        aC.put(heads[k], aCursor.getString(k) ); break;
                    case Cursor.FIELD_TYPE_BLOB:
                        aC.put(heads[k], new String(aCursor.getBlob(k)) ); break;
                    default:
                        aC.put(heads[k],  "0");
                        break;
                }
            }
            String dd=aC.getAsString("event_time");
            String tt=aC.getAsString("message");
            String status=aC.getAsString("sender");
            log.info("dbDATA" + dd + tt );
            if (selectedValues != null && selectedId<0){
                String uiDD=selectedValues.get("event_time");
                if (dd.equalsIgnoreCase(uiDD) &&
                        tt.equalsIgnoreCase(selectedValues.get("message")))
                    selectedId=aCursor.getPosition();
            }


            allRows.add(aC);
            aCursor.moveToNext();
        }
        aCursor.close();

        return allRows;
    }

    void setCommand(int forWhat){
        mCommand=forWhat;
    }
    int mCommand;

    String listViewTag="LIST_VIEW";
    View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        mContext=getActivity();
        buildListArray(mCommand);
        //getMyScreenSize();
        setRetainInstance(true);
        mRootView=inflater.inflate(R.layout.layout_for_pop, container, false);//null);

        //ScrollView sV=new ScrollView(mContext);
        //mRootView=sV;
        //sV.setFillViewport(true);
/*
        LinearLayout myUI=new LinearLayout(mContext);
        myUI.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));//mmParams);
        myUI.setOrientation(LinearLayout.VERTICAL);//0HORIZONTAL, 1Vertical);
        myUI.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);//center_horizontal
        myUI.setBackgroundColor(Color.WHITE);
*/
        TextView popTitle=(TextView)mRootView.findViewById(R.id.pop_title);//new TextView(mContext);
        String title="";
        switch (mCommand){
            case SHOW_NEWEST:
                title=getActivity().getResources().getString(R.string.action_get_recent1);
                break;
            case SHOW_FAILED1:
                title=getActivity().getResources().getString(R.string.action_get_last_failed);
                break;
            case SHOW_FAILED10:
                title=getActivity().getResources().getString(R.string.action_get_failed10);
                break;
            case SHOW_LAST10:
                title=getActivity().getResources().getString(R.string.action_get_recent10);
                break;
            case SHOW_ALL:
                title=getActivity().getResources().getString(R.string.open_log);
                break;
            default:
                title=getActivity().getResources().getString(R.string.action_get_recent10);
                break;
        }
        popTitle.setText(title);
        //popTitle.setTextSize(20f);
        //popTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
               // LinearLayout.LayoutParams.WRAP_CONTENT));
        //myUI.addView(popTitle);
        Button dismiss=(Button)mRootView.findViewById(R.id.pop_anchor);//new Button(mContext);//mRootView.findViewById(R.id.pop_anchor);
        dismiss.setText("OK");
        //dismiss.setTextSize(40f);
        //dismiss.setBackgroundColor(Color.GREEN);
        //dismiss.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
               // LinearLayout.LayoutParams.WRAP_CONTENT));

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain();
            }
        });
        //myUI.addView(dismiss);

        ListView v=(ListView)mRootView.findViewById(R.id.listLog);//new ListView(getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.layout_one_line_text, allMessages);
        v.setAdapter(adapter);
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
        v.setOnItemClickListener(null);
        v.setSelection(0);
        v.setBackgroundColor(0xffffffff);
        //v.setLayoutParams(new LinearLayout.LayoutParams(mDisplayWidth * 4 / 5, mDisplayHeight * 3 / 4, 1));
        //v.setTag(listViewTag);
        //myUI.addView(v);

        //sV.addView(myUI);

        // disable buttons on main UI which lies underneath

        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        //if (mRootView==null) return;
       //ListView v=(ListView)mRootView.findViewWithTag(listViewTag);
       //v.requestFocus();

    }

    int szKeySize;
    boolean isLargeScreen;
    boolean isLandScape;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int keyPadWidth;

    static String application_name="MainActivity";
    static String package_name="com.prod.intelligent7.engineautostart";
    static final String DAEMON="DAEMON";
    static String fileName=MainActivity.package_name+".profile";

    static String MCU_NAME=null;


    public String getSavedString(String key){
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        return mSPF.getString(key, "--");
    }
}
