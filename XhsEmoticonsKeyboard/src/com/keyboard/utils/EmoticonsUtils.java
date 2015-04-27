package com.keyboard.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.keyboard.bean.EmoticonBean;
import com.keyboard.bean.EmoticonSetBean;
import com.keyboard.db.DBHelper;
import com.keyboard.utils.imageloader.ImageBase;

import java.io.IOException;
import java.util.ArrayList;

public class EmoticonsUtils {

    /**
     * 初始化表情数据库
     * @param context
     */
    public static void initEmoticonsDB(final Context context) {
        if (!Utils.isInitDb(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBHelper dbHelper = new DBHelper(context);

                    /**
                     * FROM DRAWABLE
                     */
//                    ArrayList<EmoticonBean> emojiArray = ParseData(DefEmoticons.emojiArray, EmoticonBean.FACE_TYPE_NOMAL, ImageBase.Scheme.DRAWABLE);
//                    EmoticonSetBean emojiEmoticonSetBean = new EmoticonSetBean("emoji", 3, 7);
//                    emojiEmoticonSetBean.setIconUri("drawable://emoji_0x1f603");
//                    emojiEmoticonSetBean.setItemPadding(20);
//                    emojiEmoticonSetBean.setVerticalSpacing(10);
//                    emojiEmoticonSetBean.setShowDelBtn(true);
//                    emojiEmoticonSetBean.setEmoticonList(emojiArray);
//                    dbHelper.insertEmoticonSet(emojiEmoticonSetBean);

                    /**
                     * FROM ASSETS
                     */
                    ArrayList<EmoticonBean> xhsfaceArray = ParseData(xhsemojiArray, EmoticonBean.FACE_TYPE_NOMAL, ImageBase.Scheme.DRAWABLE);
                    EmoticonSetBean xhsEmoticonSetBean = new EmoticonSetBean("xhs", 3, 7);
                    xhsEmoticonSetBean.setIconUri("drawable://expression_1");
                    xhsEmoticonSetBean.setItemPadding(20);
                    xhsEmoticonSetBean.setVerticalSpacing(10);
                    xhsEmoticonSetBean.setShowDelBtn(true);
                    xhsEmoticonSetBean.setEmoticonList(xhsfaceArray);
                    dbHelper.insertEmoticonSet(xhsEmoticonSetBean);

                    /**
                     * FROM FILE
                     */
                    String filePath = Environment.getExternalStorageDirectory() + "/wxemoticons";
                    try{
                        FileUtils.unzip(context.getAssets().open("wxemoticons.zip"), filePath);
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    XmlUtil xmlUtil = new XmlUtil(context);
                    EmoticonSetBean bean =  xmlUtil.ParserXml(xmlUtil.getXmlFromSD(filePath + "/wxemoticons.xml"));
                    bean.setItemPadding(20);
                    bean.setVerticalSpacing(5);
                    bean.setIconUri("file://" + filePath + "/icon_030_cover.png");
                    dbHelper.insertEmoticonSet(bean);

                    /**
                     * FROM HTTP/HTTPS
                     */


                    /**
                     * FROM CONTENT
                     */


                    /**
                     * FROM USER_DEFINED
                     */

                    dbHelper.cleanup();
                    Utils.setIsInitDb(context, true);
                }
            }).start();
        }
    }

    public static EmoticonsKeyboardBuilder getSimpleBuilder(Context context) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<EmoticonSetBean> mEmoticonSetBeanList = dbHelper.queryAllEmoticonSet();
        dbHelper.cleanup();

        ArrayList<AppBean> mAppBeanList = new ArrayList<AppBean>();
        String[] funcArray = context.getResources().getStringArray(com.keyboard.view.R.array.apps_func);
        String[] funcIconArray = context.getResources().getStringArray(com.keyboard.view.R.array.apps_func_icon);
        for (int i = 0; i < funcArray.length; i++) {
            AppBean bean = new AppBean();
            bean.setId(i);
            bean.setIcon(funcIconArray[i]);
            bean.setFuncName(funcArray[i]);
            mAppBeanList.add(bean);
        }

        return new EmoticonsKeyboardBuilder.Builder()
                .setEmoticonSetBeanList(mEmoticonSetBeanList)
                .build();
    }

    public static EmoticonsKeyboardBuilder getBuilder(Context context) {

        DBHelper dbHelper = new DBHelper(context);
        ArrayList<EmoticonSetBean> mEmoticonSetBeanList = dbHelper.queryAllEmoticonSet();
        dbHelper.cleanup();

        ArrayList<AppBean> mAppBeanList = new ArrayList<AppBean>();
        String[] funcArray = context.getResources().getStringArray(com.keyboard.view.R.array.apps_func);
        String[] funcIconArray = context.getResources().getStringArray(com.keyboard.view.R.array.apps_func_icon);
        for (int i = 0; i < funcArray.length; i++) {
            AppBean bean = new AppBean();
            bean.setId(i);
            bean.setIcon(funcIconArray[i]);
            bean.setFuncName(funcArray[i]);
            mAppBeanList.add(bean);
        }

        return new EmoticonsKeyboardBuilder.Builder()
                .setEmoticonSetBeanList(mEmoticonSetBeanList)
                .build();
    }

    public static ArrayList<EmoticonBean> ParseData(String[] arry, long eventType, ImageBase.Scheme scheme) {
        try {
            ArrayList<EmoticonBean> emojis = new ArrayList<EmoticonBean>();
            for (int i = 0; i < arry.length; i++) {
                if (!TextUtils.isEmpty(arry[i])) {
                    String temp = arry[i].trim().toString();
                    String[] text = temp.split(",");
                    if (text != null && text.length == 2) {
                        String fileName = null;
                        if (scheme == ImageBase.Scheme.DRAWABLE) {
                            if(text[0].contains(".")){
                                fileName = scheme.toUri(text[0].substring(0, text[0].lastIndexOf(".")));
                            }
                            else {
                                fileName = scheme.toUri(text[0]);
                            }
                        } else {
                            fileName = scheme.toUri(text[0]);
                        }
                        String content = text[1];
                        EmoticonBean bean = new EmoticonBean(eventType, fileName, content);
                        emojis.add(bean);
                    }
                }
            }
            return emojis;
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }

        return null;
    }

    /*
    小红书表情
     */
    public static String[] xhsemojiArray = {
            "expression_1.png,[微笑]",
            "expression_2.png,[撇嘴]",
            "expression_3.png,[色]",
            "expression_4.png,[发呆]",
            "expression_5.png,[得意]",
            "expression_6.png,[流泪]",
            "expression_7.png,[害羞]",
            "expression_8.png,[闭嘴]",
            "expression_9.png,[睡]",
            "expression_10.png,[大哭]",
            "expression_11.png,[尴尬]",
            "expression_12.png,[发怒]",
            "expression_13.png,[调皮]",
            "expression_14.png,[呲牙]",
            "expression_15.png,[惊讶]",
            "expression_16.png,[难过]",
            "expression_17.png,[酷]",
            "expression_18.png,[冷汗]",
            "expression_19.png,[抓狂]",
            "expression_20.png,[吐]",
            "expression_21.png,[偷笑]",
            "expression_22.png,[愉快]",
            "expression_23.png,[白眼]",
            "expression_24.png,[傲慢]",
            "expression_25.png,[饥饿]",
            "expression_26.png,[困]",
            "expression_27.png,[惊恐]",
            "expression_28.png,[流汗]",
            "expression_29.png,[憨笑]",
            "expression_30.png,[悠闲]",
            "expression_31.png,[奋斗]",
            "expression_32.png,[咒骂]",
            "expression_33.png,[疑问]",
            "expression_34.png,[嘘]",
            "expression_35.png,[晕]",
            "expression_36.png,[疯了]",
            "expression_37.png,[衰]",
            "expression_38.png,[骷髅]",
            "expression_39.png,[敲打]",
            "expression_40.png,[再见]",
            "expression_41.png,[擦汗]",
            "expression_42.png,[抠鼻]",
            "expression_43.png,[鼓掌]",
            "expression_44.png,[糗大了]",
            "expression_45.png,[坏笑]",
            "expression_46.png,[左哼哼]",
            "expression_47.png,[右哼哼]",
            "expression_48.png,[哈欠]",
            "expression_49.png,[鄙视]",
            "expression_50.png,[委屈]",
            "expression_51.png,[快哭了]",
            "expression_52.png,[阴险]",
            "expression_53.png,[亲亲]",
            "expression_54.png,[吓]",
            "expression_55.png,[可怜]",
            "expression_56.png,[菜刀]",
            "expression_57.png,[西瓜]",
            "expression_58.png,[啤酒]",
            "expression_59.png,[篮球]",
            "expression_60.png,[乒乓]",
            "expression_61.png,[咖啡]",
            "expression_62.png,[饭]",
            "expression_63.png,[猪头]",
            "expression_64.png,[玫瑰]",
            "expression_65.png,[凋谢]",
            "expression_66.png,[嘴唇]",
            "expression_67.png,[爱心]",
            "expression_68.png,[心碎]",
            "expression_69.png,[蛋糕]",
            "expression_70.png,[闪电]",
            "expression_71.png,[炸弹]",
            "expression_72.png,[刀]",
            "expression_73.png,[足球]",
            "expression_74.png,[瓢虫]",
            "expression_75.png,[便便]",
            "expression_76.png,[月亮]",
            "expression_77.png,[太阳]",
            "expression_78.png,[礼物]",
            "expression_79.png,[拥抱]",
            "expression_80.png,[强]",
            "expression_81.png,[弱]",
            "expression_82.png,[握手]",
            "expression_83.png,[胜利]",
            "expression_84.png,[抱拳]",
            "expression_85.png,[勾引]",
            "expression_86.png,[拳头]",
            "expression_87.png,[差劲]",
            "expression_88.png,[爱你]",
            "expression_89.png,[NO]",
            "expression_90.png,[OK]",
            "expression_91.png,[爱情]",
            "expression_92.png,[飞吻]",
            "expression_93.png,[跳跳]",
            "expression_94.png,[发抖]",
            "expression_95.png,[怄火]",
            "expression_96.png,[转圈]",
            "expression_97.png,[磕头]",
            "expression_98.png,[回头]",
            "expression_99.png,[跳绳]",
            "expression_100.png,[投降]",
            "expression_101.png,[激动]",
            "expression_102.png,[乱舞]",
            "expression_103.png,[献吻]",
            "expression_104.png,[左太极]",
            "expression_105.png,[右太极]"
    };
}
