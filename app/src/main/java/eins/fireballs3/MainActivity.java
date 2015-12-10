package eins.fireballs3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    MenuItem search_menu;
    WebView mainWeb;
    int defaultPage;
    boolean exitLock;
    boolean cancelBackable;
    HttpThread versionThread;
    private long backKeyPressedTime=0;
    static SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setUp();
        initWebView();
       // Toast.makeText(getApplicationContext(),defaultPage+ "debug",Toast.LENGTH_SHORT).show();
        if(defaultPage!=-1)
        {
            SelectMenu(defaultPage);
        }

    }

    private void setUp() {
        versionThread = new HttpThread(getResources().getString(R.string.version_checker_url));
        versionThread.start();

        defaultPage=sharedPreferences.getInt("DefaultPage",-1);
        exitLock=sharedPreferences.getBoolean("exitLock", false);
        cancelBackable=sharedPreferences.getBoolean("cancelBackable",false);
        setSupportActionBar(toolbar);//툴바를 현제 액티비티의 액션바에 사용한다고 등록함
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  Toast.makeText(getApplicationContext(), position + " is clicked", Toast.LENGTH_SHORT).show();
                SelectMenu(position);
                drawerLayout.closeDrawer(listView);
            }
        });
        listView.setAdapter(arrayAdapter);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_search:
                        //TODO: 탐색바 온클릭 리스너 설정
                        /*
                        if(search_menu.isActionViewExpanded()) {
                            search_menu.collapseActionView();
                            Toast.makeText(getApplicationContext(), "열려있는경우", Toast.LENGTH_LONG).show();
                        }
                        else{
                            search_menu.expandActionView();
                            Toast.makeText(getApplicationContext(), "닫혀있는경우", Toast.LENGTH_LONG).show();
                        }

                        */
                        Log.e("onMenuItemClick#", "case R.id.action_search");
                        break;
                    case R.id.action_back:
                        if(mainWeb.canGoBack()){
                            mainWeb.goBack();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "뒤로갈 페이지가 없습니다.", Toast.LENGTH_LONG).show();
                        }

                        break;

                    case R.id.info:
                        Toast.makeText(getApplicationContext(), R.string.app_info, Toast.LENGTH_LONG).show();
                        break;

                    case R.id.setting:
                        //Toast.makeText(getApplicationContext(),"Setting",Toast.LENGTH_SHORT).show();
                        openSettingWindow();
                        break;
                    case R.id.exit:
                        Toast.makeText(getApplicationContext(), "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        break;

                    case R.id.version:

                        float curVer = Float.valueOf(getResources().getString(R.string.version));
                        int curYear = Integer.parseInt(getResources().getString(R.string.year));
                        int curMon = Integer.parseInt(getResources().getString(R.string.month));
                        int curDay = Integer.parseInt(getResources().getString(R.string.date));

                        float recVer = Float.valueOf(versionThread.getValue("version"));
                        int recYear = Integer.parseInt(versionThread.getValue("year"));
                        int recMon = Integer.parseInt(versionThread.getValue("month"));
                        int recDay = Integer.parseInt(versionThread.getValue("date"));
                        String phone = versionThread.getValue("phone");
                        final String downPath = versionThread.getValue("downloadPath");

                        String verInfo = "현재버전:" + String.format("%.2f", curVer) + "/날짜:" + curYear + "." + curMon + "." + curDay + "\n최신버전:"
                                + String.format("%.2f", recVer) + "/날짜:" + recYear + "." + recMon + "." + recDay;

                        if (curVer == recVer && curYear == recYear && curMon == recMon && curDay == recDay) {
                            //Toast.makeText(getApplicationContext(),"현재버전은 최신 버전입니다.\n"+verInfo,Toast.LENGTH_LONG).show();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("버전정보")
                                    .setMessage("현재버전은 최신 버전입니다.\n" + verInfo)
                                    .setPositiveButton("확인", null).show();
                        } else {
                            //Toast.makeText(getApplicationContext(),"최신버전의 업데이트가 있습니다.\n"+verInfo,Toast.LENGTH_LONG).show();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("버전정보-업데이트 알림")
                                    .setMessage("최신버전의 업데이트가 있습니다.\n" + verInfo)
                                    .setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            Uri u = Uri.parse(downPath);
                                            i.setData(u);
                                            startActivity(i);
                                        }
                                    })
                                    .setNegativeButton("취소", null).show();
                        }
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), "ActionBar Undefined Menu Selection", Toast.LENGTH_SHORT).show();
                        Log.e("Toolbar", "Undefined Menu selected error");
                }
                return false;
            }
        });

    }

    private void openSettingWindow() {
        final LinearLayout linear = (LinearLayout) View.inflate(this,R.layout.setting_window, null);
        ArrayAdapter<String> adspin =  new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, menuItems);
        final Spinner spin = (Spinner) linear.findViewById(R.id.default_page_spinner);
        spin.setAdapter(adspin);
        spin.setSelection(defaultPage);

        final CheckBox chkbx_exitlock = (CheckBox)linear.findViewById(R.id.chkbx_exitlock);
        final CheckBox chkbx_cancelbackable = (CheckBox)linear.findViewById(R.id.chkbx_cancelbackable);
        defaultPage = spin.getSelectedItemPosition();

        if(exitLock){chkbx_exitlock.setChecked(true);}
        else{chkbx_exitlock.setChecked(false);}

        if(cancelBackable){chkbx_cancelbackable.setChecked(true);}
        else{chkbx_cancelbackable.setChecked(false);}
        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setView(linear)
                .setPositiveButton("적용", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String output = "시작기본페이지: " + menuItems[spin.getSelectedItemPosition()] + "\n";


                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("DefaultPage", spin.getSelectedItemPosition());
                        if (chkbx_exitlock.isChecked()) {
                            editor.putBoolean("exitLock", true);
                            exitLock=true;
                            output+="취소키 종료잠금: On\n";
                        } else {
                            editor.putBoolean("exitLock", false);
                            exitLock=false;
                            output+="취소키 종료잠금: Off\n";
                        }
                        if (chkbx_cancelbackable.isChecked()) {
                            editor.putBoolean("cancelBackable", true);
                            cancelBackable=true;
                            output+="취소키 뒤로가기: On";
                        } else {
                            editor.putBoolean("cancelBackable", false);
                            cancelBackable=false;
                            output+="취소키 뒤로가기: Off";
                        }

                        editor.commit();
                        output+=" 로 설정되었습니다.";

                        Toast.makeText(getApplicationContext(), output,Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소",null).show();
    }

    public void initWebView()
    {
        WebSettings set = mainWeb.getSettings();
        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        set.setDefaultTextEncodingName("EUC-KR");
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        mainWeb.setWebChromeClient(new WebChromeClient());
        //자바스크립트 alert창 띄우기
        mainWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().indexOf("namoweb") >= 0) {
                    //host명에 namoweb이 포함되면
                    return false;
                    //해당 엡의 webView에서 처리함
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                //새 브라우저를 띄워서 처리함
                return true;
                //webView에선 처리하지 않음

            }
        });
    }

    private void initView() {
        sharedPreferences = getSharedPreferences("SavedSettings",MODE_PRIVATE);
        mainWeb=(WebView) findViewById(R.id.mainWeb);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.listView);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,menuItems);
    }

    @Override
    public void onBackPressed() {//취소 버튼을 눌렀을때
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {//drawer가 열려있으면 drawer를 닫는다
            drawerLayout.closeDrawer(listView);
        }
        else if(mainWeb.canGoBack() && cancelBackable){
            mainWeb.goBack();//웹뷰에서 뒤로가기가 가능하면 뒤로간다
        }else{
            if(exitLock) {
                Toast.makeText(getApplicationContext(), "앱을 종료하시려면 상단 툴바의 오른쪽 매뉴-Quit를 누르세요.", Toast.LENGTH_SHORT).show();
            }
            else {
                if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis();
                    Toast.makeText(getApplicationContext(),"\'뒤로\'키를 한번 더 누르시면 종료됩니다",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                    finish();
                }
            }
        }
    }



    private void SelectMenu(int position) {
      //  toolbar.setTitle(menuItems[position]);
        getSupportActionBar().setTitle(menuItems[position]);
       // Toast.makeText(getApplicationContext(),"debug: SelectMenu 함수 호출.",Toast.LENGTH_SHORT).show();
        switch(position){
            case 0:
                mainWeb.loadUrl("http://fireballs.namoweb.net/main/menu/login.html");
                break;
            case 1:
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=notice");
                break;
            case 2:
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=freeboard2");
                break;
            case 3:
                mainWeb.loadUrl("http://fireballs.namoweb.net/intro.php");
                break;
            case 4://intro
                mainWeb.loadUrl("http://fireballs.namoweb.net/main/about/about.html");
                break;
            case 5://history
                mainWeb.loadUrl("http://fireballs.namoweb.net/record.php");
                break;
            case 6://Constituion
                mainWeb.loadUrl("http://fireballs.namoweb.net/main/about/constitution.html");
                break;
            case 7: //OB board
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=ob");
                break;
            case 8: //gallery
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=fgallery");
                break;
            case 9://Aftermatch
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=strategy");
                break;
            case 10: //pds
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=vod");
                break;
            case 11://account
                mainWeb.loadUrl("http://fireballs.namoweb.net/account.php");
                break;
            default://web document
                mainWeb.loadUrl("http://fireballs.namoweb.net/bbs/zboard.php?id=webdoc");
                break;

        }
    }

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ListView listView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ArrayAdapter<String> arrayAdapter;
    String[] menuItems = new String[]{"Login","Notice","Freeboard","Members","Intro","History","Constitution","OB board","Gallery","Aftermatch","Pds","Account","Web Document"};
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        //TODO: onCreateOptionMenu 관련

        search_menu =  menu.findItem(R.id.action_search);
        if(search_menu == null) {
            Log.e("#search_menu", "is null");
        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search_menu);
               //TODO: 검색기능 추가하자
        if (searchView == null) {

            Log.e("#searchView","is null");
        }
        MenuItemCompat.setOnActionExpandListener(search_menu, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.e("#onMenu","Expand");
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //mainWeb.clearMatches();
                Log.e("#onMenu","Collapse");
                return true;
            }
        });

        assert searchView != null;
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mainWeb.findAllAsync(query);
                    searchView.requestFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mainWeb.clearMatches();
                    return true;
                }
            });
        }


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }


}
