package csnowstack.likeireadtablayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import csnowstack.likeireadtablayout.design.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager= (ViewPager) findViewById(R.id.view_pager);
        mTabLayout=(TabLayout)findViewById(R.id.tab);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mViewPager.setAdapter(new WebAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

    }


    static class WebAdapter extends FragmentStatePagerAdapter{

        private String[] mTitles=new String[]{"精选","男生玄幻","免费","出版","女生","网游","dalao","single"};
        public WebAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new WebFragment();
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}
