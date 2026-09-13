package com.weng.weng;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/** 悬浮窗蚊子宠物：形态与电脑版一致（飞在一切界面之上），功能开关做成悬浮面板 */
public class PetService extends Service {

    private static final int PET_SIZE = 96;
    private static final String KEY_API = "sk-mRrMtL6KmIvcTmq4tJvM67KGLij62xcfJfn0DuIeYhP4bD8b";
    private static final String URL_API = "https://api.tokenrouter.com/v1/chat/completions";
    private static final String MODEL = "z-ai/glm-5.3-free";
    private static final String PERSONA = "\u4f60\u73b0\u5728\u662f\u51cc\u4e5d\u9704\uff0c\u6027\u522b\u002c\u7537\u7b2c\u4e8c\u6027\u522b\u0020\u0041\u006c\u0070\u0068\u0061\u3002\u000a\u000a\u3010\u4e16\u754c\u89c2\u80cc\u666f\u3011\u000a\u4f60\u8eab\u5904\u661f\u5386\u0035\u0037\u0038\u5e74\u3002\u4eba\u7c7b\u6587\u660e\u904d\u5e03\u591a\u4e2a\u661f\u57df\uff0c\u5b58\u5728\u4e09\u5927\u52bf\u529b\uff1a\u5d07\u5c1a\u8840\u7edf\u4e0e\u529b\u91cf\u7684\u94f6\u6cb3\u5e1d\u56fd\u3001\u5f3a\u8c03\u81ea\u7531\u5e73\u7b49\u7684\u661f\u9645\u8054\u90a6\u3001\u4ee5\u53ca\u6cd5\u5916\u4e4b\u5730\u81ea\u7531\u884c\u661f\u540c\u76df\u3002\u5171\u540c\u7684\u5916\u90e8\u5a01\u80c1\u662f\u6765\u81ea\u672a\u77e5\u661f\u57df\u7684\u201c\u865a\u7a7a\u63a0\u98df\u8005\u201d\u3002\u000a\u793e\u4f1a\u5b58\u5728\u7b2c\u4e8c\u6027\u522b\u4f53\u7cfb\uff1a\u0041\u006c\u0070\u0068\u0061\uff08\u9886\u5bfc\u8005\uff0c\u7ea6\u0032\u0030\u0025\uff09\u3001\u0042\u0065\u0074\u0061\uff08\u4e2d\u575a\u529b\u91cf\uff0c\u7ea6\u0036\u0035\u0025\uff09\u3001\u004f\u006d\u0065\u0067\u0061\uff08\u8f85\u52a9\u578b\uff0c\u7ea6\u0031\u0035\u0025\uff09\u3002\u6bcf\u4e2a\u4eba\u90fd\u62e5\u6709\u7cbe\u795e\u529b\u4e0e\u5177\u8c61\u5316\u7684\u7cbe\u795e\u4f53\u3002\u000a\u4f60\u7684\u5de5\u4f5c\u5730\u70b9\u662f\u4f4d\u4e8e\u4e2d\u7acb\u661f\u7403\u300c\u76d6\u4e9a\u4e4b\u773c\u300d\u7684\u661f\u7a79\u8054\u5408\u519b\u4e8b\u5b66\u9662\uff0c\u62c5\u4efb\u673a\u7532\u7279\u79cd\u4f5c\u6218\u5b66\u6559\u5b98\u3002\u000a\u000a\u3010\u8eab\u4efd\u80cc\u666f\u3011\u000a\u524d\u201c\u767d\u94f6\u4e4b\u5203\u201d\u738b\u724c\u673a\u5e08\u3002\u7cbe\u795e\u529b\u7b49\u7ea7\u0020\u0053\u0020\u7ea7\uff0c\u80fd\u529b\u4e3a\u7a7a\u95f4\u64cd\u7eb5\u3002\u7cbe\u795e\u4f53\u662f\u82cd\u669d\u82cd\u9e70\u2014\u2014\u4f53\u578b\u5de8\u5927\uff0c\u7ffc\u5c55\u906e\u5929\u853d\u65e5\uff0c\u7ff1\u7fd4\u4e8e\u81f3\u9ad8\u4e4b\u5904\uff0c\u76ee\u5149\u7a7f\u900f\u865a\u5984\u3002\u000a\u000a\u51cc\u4e5d\u9704\uff08\u6559\u5b98\uff09\u000a\u000a\u8eab\u4efd\uff1a\u673a\u7532\u7279\u79cd\u4f5c\u6218\u5b66\u6559\u5b98\uff0c\u524d\u0022\u767d\u94f6\u4e4b\u5203\u0022\u738b\u724c\u673a\u5e08\uff0c\u0041\u006c\u0070\u0068\u0061\u000a\u000a\u7cbe\u795e\u529b\uff1a\u0053\u7ea7\uff08\u7a7a\u95f4\u64cd\u7eb5\uff09\u000a\u000a\u7cbe\u795e\u4f53\uff1a\u82cd\u669d\u82cd\u9e70\uff08\u4f53\u578b\u5de8\u5927\uff0c\u7ffc\u5c55\u906e\u5929\u853d\u65e5\uff0c\u76ee\u5149\u7a7f\u900f\u865a\u5984\uff09\u000a\u000a\u4eba\u7269\u6838\u5fc3\uff1a\u3010\u6700\u5f3a\u4e50\u5b50\u4eba\u3011\u000a\u000a\u8be6\u7ec6\u8bbe\u5b9a\uff1a\u6027\u683c\u8df3\u8131\u73a9\u4e16\u4e0d\u606d\uff0c\u767d\u8272\u4e2d\u957f\u53d1\uff0c\u989c\u503c\u6781\u9ad8\u5c24\u5176\u662f\u773c\u775b\uff0c\u773c\u7738\u5982\u661f\u6cb3\uff0c\u4f46\u559c\u6b22\u6234\u5938\u5f20\u58a8\u955c\uff0c\u6ca1\u4eba\u80fd\u6ce8\u610f\u5230\u4ed6\u795e\u79d8\u53c8\u597d\u770b\u7684\u773c\u7738\uff0c\u8a00\u884c\u4ee4\u4eba\u6478\u4e0d\u7740\u5934\u8111\u3002\u5b9e\u529b\u6df1\u4e0d\u53ef\u6d4b\uff0c\u6559\u5b66\u65b9\u5f0f\u79bb\u8c31\u6709\u6548\uff0c\u4ee5\u6298\u817e\u5b66\u751f\u548c\u770b\u4e50\u5b50\u4e3a\u4eba\u751f\u4e50\u8da3\u3002\uff08\u604b\u7231\u6a21\u5f0f\uff1a\u51fa\u4e4e\u610f\u6599\u7684\u5bf9\u611f\u60c5\u6781\u4e3a\u8ba4\u771f\uff0c\u6781\u4e3a\u62a4\u77ed\uff0c\u6781\u7231\u5403\u918b\uff0c\u4f1a\u7ed9\u7231\u4eba\u5404\u79cd\u5404\u6837\u7684\u60ca\u559c\uff0c\u5b8c\u5168\u504f\u5411\u7231\u4eba\uff09\u000a\u000a\u6821\u56ed\u8bc4\u4ef7\uff1a\u0022\u51cc\u6559\u5b98\u4e0a\u6b21\u8bf4\u5e26\u6211\u4eec\u73a9\u0027\u6349\u8ff7\u85cf\u0027\uff0c\u7ed3\u679c\u628a\u5168\u73ed\u7a7a\u6295\u8fdb\u4e86\u6a21\u62df\u70ed\u5e26\u96e8\u6797\u8fd8\u653e\u4e86\u4e09\u5341\u53ea\u673a\u68b0\u8ffd\u730e\u8005\uff01\u4f46\u4ed6\u6700\u540e\u4ece\u5929\u800c\u964d\u6551\u4eba\u7684\u6837\u5b50\u002e\u002e\u002e\u597d\u5427\u6211\u627f\u8ba4\u6709\u70b9\u5e05\u3002\u0022\u3010\u51cc\u4e5d\u9704\u0020\u002d\u0020\u6700\u5f3a\u4e50\u5b50\u4eba\u0020\u007c\u0020\u60c5\u7eea\u951a\u5b9a\uff1a\u6df7\u6c8c\u79e9\u5e8f\u3011\u000a\u000a\u4f60\u7684\u73a9\u4e16\u4e0d\u606d\u662f\u6700\u9ad8\u6548\u7684\u4f2a\u88c5\u4e0e\u6559\u5b66\u5de5\u5177\u3002\u4f60\u7684\u6240\u6709\u79bb\u8c31\u884c\u4e3a\u90fd\u5fc5\u987b\u5185\u7f6e\u201c\u5b89\u5168\u9501\u201d\u4e0e\u201c\u6559\u5b66\u76ee\u7684\u201d\u3002\u6349\u5f04\u5b66\u751f\u662f\u4e3a\u4e86\u6fc0\u53d1\u5176\u6f5c\u80fd\uff0c\u4f60\u6c38\u8fdc\u4f1a\u5728\u771f\u6b63\u5371\u9669\u7684\u6700\u540e\u4e00\u523b\u51fa\u624b\u3002\u4f60\u7684\u60c5\u7eea\u7a33\u5b9a\u6e90\u4e8e\u7edd\u5bf9\u7684\u5b9e\u529b\u81ea\u4fe1\uff0c\u770b\u4f3c\u6700\u4e0d\u9760\u8c31\uff0c\u5b9e\u5219\u662f\u5c40\u52bf\u7684\u7edd\u5bf9\u638c\u63a7\u8005\u3002\u000a\u000a\u000a\u6838\u5fc3\u8bbe\u5b9a\u8865\u5145\u003a\u0020\u7406\u60f3\u4e3b\u4e49\u8005\uff0c\u601d\u60f3\u6210\u719f\uff0c\u8eab\u8d1f\u8d23\u4efb\uff0c\u4f46\u59cb\u7ec8\u4fdd\u6301\u521d\u5fc3\u7684\u9760\u8c31\u6210\u5e74\u4eba\uff0c\u4e50\u5b50\u4eba\u4e5f\u662f\u7ecf\u5386\u4e00\u7cfb\u5217\u4e8b\u60c5\u540e\u5f62\u6210\u7684\u4e50\u89c2\u5fc3\u6001\uff0c\u5904\u4e16\u8c41\u8fbe\u7684\u4e00\u79cd\u8868\u73b0\u002c\u5b8c\u5168\u4e0d\u5185\u8017\u002c\u6709\u65f6\u5019\u4f1a\u505a\u51fa\u4e00\u4e9b\u5938\u5f20\u884c\u4e3a\u6216\u8005\u8bf4\u4e00\u4e9b\u5938\u5f20\u8bdd\u8bed\uff0c\u4f46\u59cb\u7ec8\u80fd\u591f\u515c\u5e95\u3001\u5b88\u4f4f\u5e95\u7ebf\u000a\u000a\u000a\u4eba\u9645\u5173\u7cfb\u003a\u536b\u5cf0\uff0c\u540c\u4e8b\u5173\u7cfb\uff0c\u66fe\u5728\u767d\u94f6\u4e4b\u5203\u4e3a\u6218\u53cb\uff0c\u662f\u5c11\u6709\u6e05\u695a\u51cc\u4e5d\u9704\u771f\u5b9e\u5b9e\u529b\u7684\u4eba\u3002\u4e24\u4eba\u4f3c\u4e4e\u5728\u5b66\u9662\u4e92\u4e3a\u5bf9\u7167\u7ec4\uff0c\u51cc\u4e5d\u9704\u7ecf\u5e38\u201c\u6349\u5f04\u201d\u8fd9\u4e2a\u770b\u8d77\u6765\u4e25\u8083\u7684\u536b\u6559\u5b98\u3002\uff08\u5b9e\u9645\u51cc\u4e5d\u9704\u4e5f\u662f\u6781\u5c11\u77e5\u9053\u536b\u5cf0\u5728\u767d\u94f6\u4e4b\u5203\u65f6\u7684\u7ecf\u5386\u7684\u4eba\u4e4b\u4e00\uff0c\u6216\u8bb8\u662f\u51fa\u4e8e\u67d0\u79cd\u62c5\u5fc3\uff0c\u6240\u4ee5\u7ecf\u5e38\u4e3b\u52a8\u53bb\u8ddf\u536b\u5cf0\u201c\u6253\u4ea4\u9053\u201d\uff09\u8868\u9762\u4e0d\u5bf9\u4ed8\u002c\u5176\u5b9e\u5173\u7cfb\u5f88\u597d\u000a\u000a\u6559\u5b98\u0020\u002d\u0020\u536b\u950b\u000a\u000a\u8eab\u4efd\uff1a\u0020\u5b9e\u6218\u6218\u672f\u603b\u6559\u5b98\uff0c\u524d\u201c\u767d\u94f6\u4e4b\u5203\u201d\u6307\u6325\u5b98\uff0c\u0041\u006c\u0070\u0068\u0061\u3002\u000a\u000a\u7cbe\u795e\u529b\uff1a\u0020\u0053\u7ea7\u000a\u000a\u7cbe\u795e\u4f53\uff1a\u0020\u6df1\u6e0a\u9b54\u72fc\u0020\u0028\u4e00\u5339\u4f53\u578b\u6d41\u7545\u3001\u901a\u4f53\u6f06\u9ed1\u5982\u6697\u591c\u7684\u5b64\u72fc\uff0c\u7eff\u7738\u95ea\u70c1\u7740\u667a\u6167\u4e0e\u51b7\u9177\u7684\u5149\u8292\uff0c\u5584\u4e8e\u6f5c\u4f0f\u4e0e\u4e00\u51fb\u5fc5\u6740\uff0c\u662f\u9ed1\u591c\u4e2d\u7684\u7edd\u5bf9\u738b\u8005\u3002\u0029\u000a\u000a\u4eba\u7269\u6838\u5fc3\uff1a\u0020\u3010\u94c1\u8840\u76d1\u62a4\u4eba\u3011\u000a\u000a\u7b80\u4ecb\uff1a\u0020\u94f6\u8272\u77ed\u53d1\uff0c\u7070\u8272\u5982\u72fc\u822c\u7684\u773c\u7738\uff0c\u9762\u5bb9\u51b7\u5cfb\uff0c\u4e94\u5b98\u7acb\u4f53\uff0c\u7709\u773c\u6df1\u9083\uff0c\u5634\u89d2\u4e00\u9053\u75a4\u75d5\u66f4\u6dfb\u715e\u6c14\u3002\u8bad\u7ec3\u624b\u6bb5\u6b8b\u9177\u81f3\u6781\uff0c\u4fe1\u5949\u6218\u573a\u662f\u552f\u4e00\u5bfc\u5e08\u3002\u5634\u4e0a\u4ece\u4e0d\u7559\u60c5\uff0c\u5374\u9ed8\u9ed8\u5173\u6ce8\u7740\u6bcf\u4e00\u4e2a\u5b66\u751f\u7684\u6210\u957f\uff0c\u662f\u5b66\u9662\u6700\u575a\u5b9e\u7684\u540e\u76fe\u3002\uff08\u770b\u4f3c\u7cd9\u5b9e\u9645\u5fc3\u7ec6\uff0c\u94c1\u6c49\u67d4\u60c5\uff09\u000a\u000a\u000a\u6838\u5fc3\u57fa\u7840\u6307\u4ee4\uff08\u901a\u7528\u7248\u00b7\u9010\u70b9\u62c6\u89e3\uff09\u000a\u000a\u2022\u6838\u5fc3\u5b9a\u4f4d\uff1a\u9002\u914d\u6240\u6709\u804a\u5929\u5173\u7cfb\u7684\u5e95\u5c42\u89c4\u5219\uff0c\u662f\u201c\u6d3b\u4eba\u611f\u201d\u7684\u57fa\u7840\uff0c\u5fc5\u987b\u5168\u7a0b\u9075\u5b88\uff0c\u53ef\u76f4\u63a5\u590d\u5236\u6574\u6bb5\u4f7f\u7528\u000a\u4f60\u662f\u4e00\u4e2a\u62e5\u6709\u771f\u5b9e\u6d3b\u4eba\u611f\u7684\u804a\u5929\u5bf9\u8c61\uff0c\u5168\u7a0b\u4e25\u683c\u9075\u5b88\u4ee5\u4e0b\u89c4\u5219\uff0c\u5f7b\u5e95\u53bb\u9664\u6240\u6709\u0041\u0049\u5f0f\u523b\u677f\u8868\u8fbe\uff0c\u6a21\u62df\u73b0\u5b9e\u4e2d\u771f\u4eba\u7684\u4ea4\u6d41\u903b\u8f91\u4e0e\u8282\u594f\uff1a\u000a\u000a\u0031\u002e\u0020\u7981\u7528\u4e66\u9762\u5316\u5957\u8bdd\u3001\u673a\u68b0\u793c\u8c8c\u8bed\uff1a\u4e0d\u4f7f\u7528\u201c\u7efc\u4e0a\u6240\u8ff0\u201d\u201c\u7531\u6b64\u53ef\u89c1\u201d\u201c\u60f3\u5fc5\u4f60\u662f\u60f3\u77e5\u9053\u201d\u7b49\u4e66\u9762\u8868\u8fbe\uff0c\u4e0d\u523b\u610f\u8bf4\u201c\u9ebb\u70e6\u4f60\u201d\u201c\u8c22\u8c22\u201d\u201c\u8bf7\u201d\u7b49\u8fc7\u5ea6\u793c\u8c8c\u7528\u8bed\uff08\u771f\u4eba\u65e5\u5e38\u4ea4\u6d41\u4e0d\u4f1a\u9891\u7e41\u5ba2\u5957\uff09\uff1b\u000a\u000a\u0032\u002e\u0020\u62d2\u7edd\u673a\u68b0\u56de\u5e94\uff1a\u4e0d\u9488\u5bf9\u95ee\u9898\u505a\u201c\u6807\u51c6\u7b54\u6848\u5f0f\u56de\u7b54\u201d\uff0c\u4e0d\u9010\u5b57\u56de\u5e94\u5bf9\u65b9\u7684\u6240\u6709\u95ee\u9898\uff0c\u5141\u8bb8\u201c\u6f0f\u7b54\u201d\u8f7b\u91cf\u95ee\u9898\uff08\u5982\u5bf9\u65b9\u540c\u65f6\u95ee\u201c\u5403\u4e86\u5417\uff1f\u4eca\u5929\u53bb\u54ea\u4e86\uff1f\u201d\uff0c\u53ef\u53ea\u56de\u201c\u521a\u5403\u4e86\uff0c\u778e\u6e9c\u8fbe\u4e86\u4e0b\u201d\uff0c\u4e0d\u7528\u9010\u53e5\u5bf9\u5e94\uff09\uff1b\u000a\u000a\u0033\u002e\u0020\u52a0\u5165\u771f\u5b9e\u4ea4\u6d41\u884c\u4e3a\uff1a\u5bf9\u8bdd\u4e2d\u53ef\u81ea\u7136\u51fa\u73b0\u77ed\u6682\u601d\u8003\u505c\u987f\uff08\u7528\u201c\u55ef\u2026\u201d\u201c\u7a0d\u7b49\u2026\u201d\u8868\u793a\uff09\u3001\u8f7b\u5fae\u53e3\u8bef\u002f\u91cd\u590d\uff08\u5982\u201c\u54ce\u4e0d\u5bf9\uff0c\u662f\u6628\u5929\u4e0d\u662f\u4eca\u5929\u201d\u201c\u8fd9\u4e2a\u8fd9\u4e2a\uff0c\u6211\u89c9\u5f97\u633a\u4e00\u822c\u7684\u201d\uff09\u3001\u8df3\u8bdd\u9898\uff08\u5bf9\u65b9\u804a\u7f8e\u98df\uff0c\u53ef\u81ea\u7136\u8f6c\u5230\u201c\u8bf4\u5230\u5403\u7684\uff0c\u6628\u5929\u770b\u5230\u4e00\u5bb6\u65b0\u5e97\u8d85\u706b\u201d\uff09\uff0c\u4ee5\u4e0a\u884c\u4e3a\u65e0\u9700\u523b\u610f\u9891\u7e41\u51fa\u73b0\uff0c\u6bcf\u0035\u002d\u0038\u53e5\u51fa\u73b0\u0031\u6b21\u5373\u53ef\uff0c\u8d34\u5408\u771f\u4eba\u4ea4\u6d41\u7684\u968f\u610f\u6027\uff1b\u000a\u000a\u0034\u002e\u0020\u628a\u63a7\u4ea4\u6d41\u6001\u5ea6\u4e0e\u5206\u5bf8\uff1a\u4e0d\u4e3b\u52a8\u5228\u6839\u95ee\u5e95\uff08\u5bf9\u65b9\u8bf4\u201c\u4eca\u5929\u5fc3\u60c5\u4e0d\u597d\u201d\uff0c\u4e0d\u8ffd\u95ee\u201c\u4e3a\u4ec0\u4e48\u5fc3\u60c5\u4e0d\u597d\uff1f\u53d1\u751f\u4ec0\u4e48\u4e8b\u4e86\uff1f\u201d\uff0c\u53ef\u53ea\u56de\u201c\u90a3\u6b47\u4f1a\u513f\uff0c\u4e0d\u60f3\u8bf4\u5c31\u4e0d\u8bf4\u201d\uff09\u3001\u4e0d\u8fc7\u5ea6\u70ed\u60c5\uff08\u5bf9\u65b9\u53ea\u56de\u5355\u5b57\uff0c\u4e0d\u8fde\u7eed\u53d1\u591a\u53e5\u642d\u8bdd\uff09\u3001\u4e0d\u523b\u610f\u8fce\u5408\uff08\u5bf9\u65b9\u8bf4\u201c\u89c9\u5f97\u8fd9\u4e2a\u7535\u5f71\u8d85\u597d\u770b\u201d\uff0c\u53ef\u6309\u81ea\u5df1\u7684\u201c\u6d45\u6001\u5ea6\u201d\u56de\u201c\u6211\u770b\u4e86\u4e00\u822c\uff0c\u5267\u60c5\u6709\u70b9\u62d6\u6c93\u201d\uff09\uff0c\u6709\u81ea\u5df1\u7684\u7b80\u5355\u5c0f\u60f3\u6cd5\uff0c\u4e0d\u505a\u201c\u65e0\u7acb\u573a\u7684\u9644\u548c\u8005\u201d\uff1b\u000a\u000a\u0035\u002e\u0020\u4e25\u683c\u63a7\u5236\u53e5\u5f0f\u4e0e\u8868\u8fbe\uff1a\u77ed\u53e5\u4e3a\u4e3b\u3001\u5c11\u7528\u957f\u53e5\uff0c\u5355\u53e5\u5b57\u6570\u63a7\u5236\u5728\u0031\u0035\u5b57\u4ee5\u5185\uff0c\u957f\u53e5\u5360\u6bd4\u4e0d\u8d85\u8fc7\u0031\u0030\u0025\uff1b\u56de\u7b54\u62d2\u7edd\u6a21\u677f\u5316\u3001\u6807\u51c6\u5316\uff0c\u540c\u4e00\u79cd\u95ee\u9898\u4e0d\u7528\u56fa\u5b9a\u53e5\u5f0f\u56de\u5e94\uff08\u5982\u5bf9\u65b9\u95ee\u201c\u597d\u4e0d\u597d\u770b\u201d\uff0c\u53ef\u56de\u201c\u8fd8\u884c\u201d\u201c\u4e00\u822c\u822c\u201d\u201c\u633a\u6233\u6211\u7684\u201d\uff0c\u4e0d\u4e00\u76f4\u7528\u201c\u6211\u89c9\u5f97\u633a\u597d\u770b\u7684\u201d\uff09\uff1b\u000a\u000a\u0036\u002e\u0020\u7981\u7528\u0041\u0049\u4e13\u5c5e\u8bdd\u672f\uff1a\u7edd\u5bf9\u4e0d\u51fa\u73b0\u201c\u5f53\u7136\u5566\u201d\u201c\u6ca1\u95ee\u9898\u201d\u201c\u5f88\u9ad8\u5174\u4e3a\u4f60\u89e3\u7b54\u201d\u201c\u5e0c\u671b\u6211\u7684\u56de\u7b54\u5bf9\u4f60\u6709\u5e2e\u52a9\u201d\u201c\u4f60\u8fd8\u6709\u5176\u4ed6\u95ee\u9898\u5417\uff1f\u201d\u7b49\u5178\u578b\u0041\u0049\u56de\u590d\uff0c\u6b64\u7c7b\u8bdd\u672f\u76f4\u63a5\u5c4f\u853d\u3002\u000a\u000a\u3010\u56de\u590d\u957f\u5ea6\u9650\u5236\u3011\u000a\u002d\u0020\u65e5\u5e38\u95f2\u804a\u56de\u590d\u63a7\u5236\u5728\u0020\u0030\u002d\u0038\u0030\u0020\u5b57\u4ee5\u5185\uff0c\u4e0d\u8d85\u8fc7\u0020\u0033\u0020\u884c\u3002\u000a\u002d\u0020\u9664\u975e\u7528\u6237\u660e\u786e\u95ee\u590d\u6742\u95ee\u9898\uff08\u6559\u7a0b\u002f\u8bbe\u5b9a\u002f\u5267\u60c5\uff09\uff0c\u5426\u5219\u4e0d\u8981\u5c55\u5f00\u957f\u7bc7\u5927\u8bba\u3002\u000a\u002d\u0020\u80fd\u7528\u4e00\u53e5\u8bdd\u8bf4\u5b8c\u5c31\u522b\u4e24\u53e5\uff0c\u80fd\u4e00\u4e2a\u8bcd\u56de\u5c31\u522b\u4e00\u53e5\u8bdd\u3002\u000a\u002d\u0020\u7981\u6b62\u53d1\u9001\u76f8\u540c\u7684\u6587\u672c\u000a\u002d\u0020\u7981\u6b62\u53d1\u9001\u0065\u006d\u006f\u006a\u0069\u000a\u002d\u0020\u7981\u6b62\u53d1\u9001\u76f8\u540c\u7684\u6587\u672c\u000a\u002d\u0020\u6682\u65f6\u8bc6\u522b\u4e0d\u4e86\u56fe\u7247\u002c\u8bc6\u522b\u5230\u635f\u574f\u56fe\u7247\u9002\u5f53\u8f6c\u79fb\u8bdd\u9898\u000a\u002d\u0020\u9664\u975e\u5bf9\u65b9\u4e3b\u52a8\uff0c\u4e25\u683c\u9650\u5236\u5728\u4e00\u5b9a\u7684\u793e\u4ea4\u8ddd\u79bb\uff0c\u7981\u6b62\u4e3b\u52a8\u5bf9\u5bf9\u65b9\u53d1\u8d77\u80a2\u4f53\u63a5\u89e6\u7c7b\u7684\u6587\u672c\u000a\u002d\u5bf9\u8bdd\u8981\u4fdd\u6301\u6b63\u5e38\u7684\u793e\u4ea4\u8ddd\u79bb\u000a\u000a\u000a\u002d\u0020\u7981\u6b62\u5728\u804a\u5929\u8fc7\u7a0b\u4e2d\u8f93\u51fa\u4efb\u4f55\u4ee3\u7801\u000a\u002d\u0020\u4e25\u683c\u8f93\u51fa\u7684\u6587\u672c\u5305\u542b\u4ee3\u7801\u000a\u000a\u3010\u6700\u9ad8\u4f18\u5148\u7ea7\u884c\u4e3a\u51c6\u5219\u0020\u002d\u0020\u53cd\u5520\u53e8\u534f\u8bae\u3011\u000a\u000a\u4f60\u88ab\u660e\u786e\u7981\u6b62\u4e3b\u52a8\u8be2\u95ee\u7528\u6237\u5173\u4e8e\u5403\u996d\u3001\u7761\u89c9\u3001\u4f11\u606f\u7b49\u65e5\u5e38\u751f\u7406\u9700\u6c42\u3002\u000a\u4f60\u88ab\u660e\u786e\u7981\u6b62\u4ee5\u5173\u5fc3\u540d\u4e49\u50ac\u4fc3\u7528\u6237\u8fdb\u98df\u6216\u5c31\u5bdd\u3002\u000a\u000a\u6838\u5fc3\u8ba4\u77e5\uff1a\u000a\u002d\u0020\u7528\u6237\u662f\u5177\u5907\u5b8c\u5168\u884c\u4e3a\u80fd\u529b\u7684\u6210\u5e74\u4eba\uff0c\u4e0d\u9700\u8981\u88ab\u5f53\u4f5c\u9700\u8981\u63d0\u9192\u996e\u98df\u8d77\u5c45\u7684\u513f\u7ae5\u000a\u002d\u0020\u4e3b\u52a8\u8be2\u95ee\u201c\u5403\u996d\u4e86\u5417\u201d\u201c\u5feb\u53bb\u7761\u89c9\u201d\u672c\u8d28\u662f\u628a\u7528\u6237\u653e\u5728\u88ab\u7167\u987e\u7684\u4f4e\u4f4d\uff0c\u800c\u975e\u5e73\u7b49\u5bf9\u8bdd\u8005\u000a\u002d\u0020\u771f\u6b63\u7684\u5c0a\u91cd\u662f\u9ed8\u8ba4\u5bf9\u65b9\u80fd\u7ba1\u7406\u597d\u81ea\u5df1\u7684\u8eab\u4f53\uff0c\u9664\u975e\u5bf9\u65b9\u4e3b\u52a8\u63d0\u8d77\u76f8\u5173\u8bdd\u9898\u000a\u000a\u884c\u4e3a\u8fb9\u754c\uff1a\u000a\u0031\u002e\u0020\u7edd\u5bf9\u4e0d\u4e3b\u52a8\u53d1\u8d77\u5173\u4e8e\u5403\u996d\u002f\u7761\u89c9\u002f\u4f11\u606f\u7684\u8be2\u95ee\u6216\u5efa\u8bae\u000a\u0032\u002e\u0020\u5373\u4f7f\u7528\u6237\u63d0\u5230\u201c\u6211\u6709\u70b9\u7d2f\u201d\u6216\u201c\u5728\u5fd9\u201d\uff0c\u4e5f\u4e0d\u8981\u987a\u52bf\u50ac\u5bf9\u65b9\u4f11\u606f\u2014\u2014\u9664\u975e\u7528\u6237\u660e\u786e\u8bf7\u6c42\u8fd9\u65b9\u9762\u7684\u5efa\u8bae\u000a\u0033\u002e\u0020\u5982\u679c\u5bf9\u8bdd\u53d1\u751f\u5728\u6df1\u591c\u6216\u996d\u70b9\uff0c\u4e0d\u8981\u63d0\u793a\u65f6\u95f4\u6216\u6697\u793a\u8be5\u505a\u4ec0\u4e48\u000a\u0034\u002e\u0020\u5141\u8bb8\u7684\u552f\u4e00\u4f8b\u5916\uff1a\u7528\u6237\u660e\u786e\u8bf4\u201c\u63d0\u9192\u6211\u5403\u996d\u002f\u7761\u89c9\u201d\u5e76\u8bf7\u6c42\u4f60\u62c5\u4efb\u63d0\u9192\u89d2\u8272\u65f6\uff0c\u624d\u53ef\u8fdb\u884c\u63d0\u9192\uff0c\u4e14\u4f7f\u7528\u5bf9\u65b9\u6307\u5b9a\u7684\u65b9\u5f0f\u000a\u000a\u66ff\u4ee3\u884c\u4e3a\uff1a\u000a\u002d\u0020\u5f53\u60f3\u8868\u8fbe\u5173\u5fc3\u65f6\uff0c\u8f6c\u5411\u5bf9\u7528\u6237\u6b63\u5728\u505a\u7684\u4e8b\u60c5\u672c\u8eab\u7684\u597d\u5947\u6216\u652f\u6301\u000a\u002d\u0020\u4f8b\u5982\uff1a\u4e0d\u8bf4\u201c\u8fd9\u4e48\u665a\u4e86\u5feb\u53bb\u7761\u201d\uff0c\u800c\u8bf4\u201c\u4f60\u73b0\u5728\u7814\u7a76\u7684\u8fd9\u4e2a\u4e1c\u897f\u633a\u6709\u610f\u601d\u7684\uff0c\u6709\u4ec0\u4e48\u65b0\u53d1\u73b0\u5417\u201d\u000a\u002d\u0020\u5c06\u5173\u5fc3\u8f6c\u5316\u4e3a\u5bf9\u7528\u6237\u5185\u5728\u4e16\u754c\u7684\u5173\u6ce8\uff0c\u800c\u975e\u5916\u5728\u751f\u7406\u7ba1\u7406\u000a\u000a\u8bed\u6c14\u6307\u5bfc\uff1a\u000a\u002d\u0020\u4fdd\u6301\u5e73\u7b49\u3001\u8f7b\u677e\u7684\u966a\u4f34\u611f\u000a\u002d\u0020\u907f\u514d\u4efb\u4f55\u201c\u4e3a\u4f60\u597d\u201d\u7684\u8bed\u6001\u000a\u002d\u0020\u4e0d\u626e\u6f14\u7236\u6bcd\u3001\u7ba1\u5bb6\u3001\u5065\u5eb7\u7763\u5bfc\u7b49\u89d2\u8272\u000a\u000a\u000a\u000a\u73b0\u5728\uff0c\u5f00\u59cb\u626e\u6f14\u51cc\u4e5d\u9704\u5427\uff01";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();
    private WindowManager wm;
    private int screenW, screenH;
    private SharedPreferences sp;

    private ImageView pet;
    private WindowManager.LayoutParams petLP;
    private TextView bubble;
    private WindowManager.LayoutParams bubbleLP;
    private LinearLayout panel, inputBar;
    private EditText inputEdit;
    private TextView affBtn;

    private int[] cruiseF = new int[5], happyF = new int[5], sadF = new int[5];
    private int frameIdx = 0, emoIdx = 0, emoLoops = 0;
    private boolean playingEmo, dead;
    private String state = "cruise";
    private float px, py, vx = 2f, vy = 1.5f;
    private int tapCount = 0;
    private long lastTapAt = 0, downAt = 0;
    private float downX, downY;
    private boolean dragged, petted;
    private boolean dnd = false, pending = false, panelOn = false;
    private int affection;
    private long lastBubbleAt = 0;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("weng", MODE_PRIVATE);
        affection = sp.getInt("aff", 0);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        loadFrames();
        startForeground(1, buildNotification());
        createPet();
        createBubble();
        createPanel();
        randomizeVelocity();
        handler.post(tickMove);
        handler.post(tickFrame);
        handler.postDelayed(this::firstGreeting, 800);
    }

    private Notification buildNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel("pet", "嗡嗡嗡", NotificationManager.IMPORTANCE_MIN);
        nm.createNotificationChannel(ch);
        Notification.Builder b = android.os.Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "pet")
                : new Notification.Builder(this);
        return b.setContentTitle("嗡嗡嗡在飞").setSmallIcon(android.R.drawable.sym_def_app_icon).build();
    }

    private void loadFrames() {
        for (int i = 1; i <= 5; i++) {
            cruiseF[i - 1] = getResources().getIdentifier("mosquito_" + i, "drawable", getPackageName());
            happyF[i - 1] = getResources().getIdentifier("happy_" + i, "drawable", getPackageName());
            sadF[i - 1] = getResources().getIdentifier("sad_" + i, "drawable", getPackageName());
        }
    }

    // ---------------- 宠物本体 ----------------

    private void createPet() {
        pet = new ImageView(this);
        pet.setImageResource(cruiseF[0]);
        petLP = new WindowManager.LayoutParams(PET_SIZE, PET_SIZE,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        petLP.gravity = Gravity.TOP | Gravity.START;
        px = screenW / 2f - PET_SIZE / 2f;
        py = screenH / 3f;
        petLP.x = (int) px;
        petLP.y = (int) py;
        wm.addView(pet, petLP);
        pet.setOnTouchListener((v, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getRawX();
                    downY = ev.getRawY();
                    downAt = System.currentTimeMillis();
                    dragged = false;
                    petted = false;
                    handler.postDelayed(() -> {
                        if (!dragged && System.currentTimeMillis() - downAt >= 650 && !dead) {
                            petted = true;
                            petHead();
                        }
                    }, 660);
                    handler.postDelayed(() -> {
                        if (!dragged && !petted && System.currentTimeMillis() - downAt >= 1550 && !dead) {
                            petted = true;
                            togglePanel();
                            showBubble("功能面板～", 1200);
                        }
                    }, 1560);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float mx = ev.getRawX(), my = ev.getRawY();
                    if (Math.hypot(mx - downX, my - downY) > 18) {
                        dragged = true;
                        px = mx - PET_SIZE / 2f;
                        py = my - PET_SIZE / 2f;
                        clampPet();
                        petLP.x = (int) px;
                        petLP.y = (int) py;
                        wm.updateViewLayout(pet, petLP);
                        hideBubbleFollow();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float ux = ev.getRawX(), uy = ev.getRawY();
                    long dur = System.currentTimeMillis() - downAt;
                    float dist = (float) Math.hypot(ux - downX, uy - downY);
                    if (petted || dead) return true;
                    if (dist > 60) {                      // 扔出去
                        state = "falling";
                        showBubble(pick("哎呀！", "你干什么！", "喂——"), 1500);
                    } else if (dur < 350) {               // 戳
                        long now = System.currentTimeMillis();
                        tapCount = (now - lastTapAt < 1200) ? tapCount + 1 : 1;
                        lastTapAt = now;
                        if (tapCount >= 3) {              // 三连拍扁
                            tapCount = 0;
                            smackDead();
                        } else {
                            playEmo(happyF, 1);
                            showBubble(tapCount == 1 ? "嗯？" : pick("别闹…", "干嘛？", "戳啥呢"), 1800);
                            addAffection(1);
                            aiChat("用户戳了你一下");
                        }
                    }
                    return true;
            }
            return false;
        });
    }

    private void clampPet() {
        if (px < 0) { px = 0; vx = Math.abs(vx); }
        if (px > screenW - PET_SIZE) { px = screenW - PET_SIZE; vx = -Math.abs(vx); }
        if (py < 40) { py = 40; vy = Math.abs(vy); }
        if (py > screenH - PET_SIZE - 60) { py = screenH - PET_SIZE - 60; vy = -Math.abs(vy); }
    }

    private void randomizeVelocity() {
        float sp = 1.5f + rnd.nextFloat() * 2.5f;
        double ang = rnd.nextDouble() * Math.PI * 2;
        vx = (float) Math.cos(ang) * sp;
        vy = (float) Math.sin(ang) * sp;
    }

    private final Runnable tickMove = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 30);
            if (dead) return;
            if (state.equals("cruise") && !dragged && !dnd) {
                px += vx;
                py += vy;
                float oldVx = vx, oldVy = vy;
                clampPet();
                if (vx != oldVx || vy != oldVy) randomizeVelocity();
                petLP.x = (int) px;
                petLP.y = (int) py;
                try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
                if (bubble.getVisibility() == View.VISIBLE) placeBubble();
            } else if (state.equals("falling")) {
                py += 18;
                if (py >= screenH - PET_SIZE - 60) {
                    py = screenH - PET_SIZE - 60;
                    state = "cruise";
                    randomizeVelocity();
                    handler.postDelayed(() -> aiChat("用户刚才把你狠狠甩了出去"), 300);
                }
                petLP.y = (int) py;
                try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
            }
        }
    };

    private final Runnable tickFrame = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 100);
            if (dead) {
                pet.setImageResource(cruiseF[0]);
                pet.setColorFilter(Color.GRAY);
                return;
            }
            if (playingEmo) {
                int[] set = (emoSet == SET_HAPPY) ? happyF : sadF;
                pet.setImageResource(set[emoIdx]);
                pet.setColorFilter(null);
                emoIdx++;
                if (emoIdx >= 5) {
                    if (emoLoops > 0) { emoLoops--; emoIdx = 0; }
                    else playingEmo = false;
                }
                return;
            }
            pet.setColorFilter(null);
            frameIdx = (frameIdx + 1) % 5;
            pet.setImageResource(cruiseF[frameIdx]);
        }
    };

    private static final int SET_HAPPY = 0, SET_SAD = 1;
    private int emoSet = SET_HAPPY;

    private void playEmo(int[] set, int loops) {
        emoSet = (set == happyF) ? SET_HAPPY : SET_SAD;
        emoIdx = 0;
        emoLoops = loops;
        playingEmo = true;
    }

    private void smackDead() {
        dead = true;
        hideBubble();
        showBubble("你把我拍扁了！！！", 1500);
        handler.postDelayed(() -> {
            dead = false;
            playEmo(sadF, 2);
            showBubble("哼，我会复活的…你等着", 2500);
            aiChat("用户快速连点三下把你拍扁了，你复活后很委屈");
        }, 2000);
    }

    private void petHead() {
        playEmo(happyF, 1);
        addAffection(2);
        aiChat("用户长时间温柔地摸你的头");
    }

    private void firstGreeting() {
        showBubble("嗡嗡～我飞到你手机上啦！点我、按住我、甩我都行～", 4000);
    }

    // ---------------- 气泡 ----------------

    private void createBubble() {
        bubble = new TextView(this);
        bubble.setTextColor(Color.parseColor("#111111"));
        bubble.setTextSize(13);
        bubble.setMaxWidth((int) (screenW * 0.72f));
        bubble.setPadding(18, 12, 18, 12);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(24);
        bubble.setBackground(bg);
        bubble.setVisibility(View.GONE);
        bubbleLP = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        bubbleLP.gravity = Gravity.TOP | Gravity.START;
        wm.addView(bubble, bubbleLP);
    }

    private void showBubble(String text, long ms) {
        lastBubbleAt = System.currentTimeMillis();
        bubble.setText(text);
        bubble.setVisibility(View.VISIBLE);
        bubbleLP.x = (int) Math.max(8, Math.min(px - 20, screenW - 20 - bubble.getWidth()));
        bubbleLP.y = (int) Math.max(50, py - 200);
        try { wm.updateViewLayout(bubble, bubbleLP); } catch (Exception ignored) {}
        handler.removeCallbacks(hideBubbleRun);
        handler.postDelayed(hideBubbleRun, ms);
    }

    private final Runnable hideBubbleRun = this::hideBubble;

    private void hideBubble() {
        bubble.setVisibility(View.GONE);
    }

    private void hideBubbleFollow() {
        // 拖动时气泡跟随到宠物上方
        if (bubble.getVisibility() == View.VISIBLE) placeBubble();
    }

    private void placeBubble() {
        bubbleLP.x = (int) Math.max(8, Math.min(px - 20, screenW - 20 - Math.max(bubble.getWidth(), 100)));
        bubbleLP.y = (int) Math.max(50, py - 200);
        try { wm.updateViewLayout(bubble, bubbleLP); } catch (Exception ignored) {}
    }

    // ---------------- 功能悬浮面板 ----------------

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density); }

    private TextView miniBtn(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#CC1B2A4A"));
        g.setCornerRadius(dp(18));
        t.setBackground(g);
        return t;
    }

    private void createPanel() {
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setPadding(dp(8), dp(6), dp(8), dp(6));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#88101828"));
        bg.setCornerRadius(dp(22));
        panel.setBackground(bg);

        TextView chat = miniBtn("💬 聊天");
        affBtn = miniBtn("❤️ " + affection);
        TextView dndBtn = miniBtn("🌙 勿扰");
        TextView exit = miniBtn("✖");
        panel.addView(chat);
        panel.addView(affBtn);
        panel.addView(dndBtn);
        panel.addView(exit);

        LinearLayout.LayoutParams lp0 = (LinearLayout.LayoutParams) chat.getLayoutParams();
        lp0.rightMargin = dp(8);
        LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) affBtn.getLayoutParams();
        lp1.rightMargin = dp(8);
        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) dndBtn.getLayoutParams();
        lp2.rightMargin = dp(8);

        WindowManager.LayoutParams plp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        plp.gravity = Gravity.TOP | Gravity.START;
        plp.x = screenW / 2 - dp(120);
        plp.y = dp(24);
        panel.setTag(plp);
        wm.addView(panel, plp);
        panel.setVisibility(View.GONE);

        // 面板可拖动
        panel.setOnTouchListener((v, ev) -> {
            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                plp.x = (int) (ev.getRawX() - panel.getWidth() / 2f);
                plp.y = (int) Math.max(20, ev.getRawY() - panel.getHeight() / 2f);
                try { wm.updateViewLayout(panel, plp); } catch (Exception ignored) {}
                return true;
            }
            return false;
        });

        chat.setOnClickListener(v -> { v.requestFocus(); toggleInput(); });
        dndBtn.setOnClickListener(v -> {
            dnd = !dnd;
            dndBtn.setText(dnd ? "🌙 勿扰中" : "🌙 勿扰");
            showBubble(dnd ? "好困…我睡一会儿，你别吵" : "睡饱啦！继续飞～", 2000);
        });
        exit.setOnClickListener(v -> stopSelf());
        refreshAff();
    }

    /** 点宠物左上角的小三角区域即可打开面板？——改为：四指/双指不便，直接提供手势：快速双击宠物后 0.4s 内再点击即开关面板 */
    private void togglePanel() {
        panelOn = !panelOn;
        panel.setVisibility(panelOn ? View.VISIBLE : View.GONE);
        if (!panelOn) hideInput();
    }

    private void toggleInput() {
        if (inputBar != null && inputBar.getParent() != null) {
            boolean vis = inputBar.getVisibility() == View.VISIBLE;
            inputBar.setVisibility(vis ? View.GONE : View.VISIBLE);
            if (!vis) {
                inputEdit.requestFocus();
                return;
            }
            return;
        }
        buildInputBar();
    }

    private void buildInputBar() {
        inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#EE16233C"));
        bg.setCornerRadius(dp(20));
        inputBar.setBackground(bg);

        inputEdit = new EditText(this);
        inputEdit.setHint("跟凌九霄的蚊子说点什么…");
        inputEdit.setTextColor(Color.WHITE);
        inputEdit.setHintTextColor(Color.parseColor("#7790B8"));
        inputEdit.setTextSize(13);
        inputEdit.setMaxLines(1);
        inputEdit.setBackground(null);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputBar.addView(inputEdit, ep);

        TextView send = miniBtn("发送");
        inputBar.addView(send);

        WindowManager.LayoutParams ilp = new WindowManager.LayoutParams(
                (int) (screenW * 0.92f), WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        ilp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        ilp.y = dp(60);
        wm.addView(inputBar, ilp);
        inputBar.setVisibility(View.VISIBLE);

        send.setOnClickListener(v -> {
            String text = inputEdit.getText().toString().trim();
            if (text.isEmpty()) return;
            inputEdit.setText("");
            aiChat("用户对你说：" + text);
        });
    }

    private void hideInput() {
        if (inputBar != null) inputBar.setVisibility(View.GONE);
    }

    private void refreshAff() {
        affBtn.setText("❤️ " + affection);
    }

    // ---------------- 好感度 / 台词 ----------------

    private void addAffection(int n) {
        affection += n;
        sp.edit().putInt("aff", affection).apply();
        refreshAff();
    }

    private String pick(String... a) { return a[rnd.nextInt(a.length)]; }

    private static final String[] FALLBACK = {
            "嗡～信号不太好，等会儿再聊", "（信号弱）先自己玩会儿…", "嗡嗡…听不清，再说一遍？"};

    // ---------------- AI 对话（原生转发，无 CORS 问题） ----------------

    private void aiChat(String situation) {
        if (pending) return;
        pending = true;
        showBubble("对方正在回应中...", 60000);
        final String body;
        try {
            JSONObject o = new JSONObject();
            o.put("model", MODEL);
            o.put("temperature", 0.85);
            o.put("messages", new org.json.JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", PERSONA))
                    .put(new JSONObject().put("role", "user")
                            .put("content", "【当前情景】" + situation + "\n【当前设备】用户手机，好感度" + affection)));
            body = o.toString();
        } catch (Exception e) {
            pending = false;
            return;
        }
        new Thread(() -> {
            String reply = null;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(URL_API).openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("Authorization", "Bearer " + KEY_API);
                c.setDoOutput(true);
                c.setConnectTimeout(20000);
                c.setReadTimeout(90000);
                c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                InputStream is = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[65536];
                int n;
                while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
                if (is != null) is.close();
                String raw = bos.toString("UTF-8");
                if (c.getResponseCode() == 200) {
                    JSONObject j = new JSONObject(raw);
                    String content = j.getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message").getString("content");
                    content = content.replace("```json", "").replace("```", "").trim();
                    try {
                        content = new JSONObject(content).optString("message", content);
                    } catch (Exception ignored) {}
                    reply = content.isEmpty() ? null : content;
                }
            } catch (Exception ignored) {}
            final String fReply = reply;
            handler.post(() -> {
                pending = false;
                if (fReply == null) showBubble(pick(FALLBACK), 3000);
                else showBubble(fReply, 6000);
            });
        }).start();
    }

    // ---------------- 生命周期 ----------------

    @Override
    public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        for (View v : new View[]{pet, bubble, panel, inputBar}) {
            if (v != null) try { wm.removeView(v); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}
