package com.weng.weng;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
/** 悬浮窗蚊子宠物：形态与电脑版一致（飞在一切界面之上），功能开关做成悬浮面板 */
public class PetService extends Service implements Flyer.Host {

    private static final int PET_SIZE = 96;
    private static final String KEY_API = "sk-mRrMtL6KmIvcTmq4tJvM67KGLij62xcfJfn0DuIeYhP4bD8b";
    private static final String URL_API = "https://api.tokenrouter.com/v1/chat/completions";
    private static final String MODEL = "z-ai/glm-5.3-free";
    private static final String[] SEED = {"leng-jiu", "xiao-gai", "a-eye-57", "8"};
    private static final String BLOB =
            "W1:fedaef78:LWkVxGunzF/9aBeINaTxsipyFysyP+UG+z2hkqoh5U2lRFLToxoHZXAPehToFicP/G5UGsKD96ghvC+6QMIN" +
            "S+i+SIy2ppdDFqgipojS/hkjG7SXHk2L+qP/tO25a6t0meom8ISYlVYZoM8b3NjxP2u/1i1CACHhw4QNCARmuIl+rGUvNJZb" +
            "KSoTIrhURiKEHEYt2TM1OLIXvff52r6ToruLid4sTIVpiM1M4uXltr61APV8K2eumbxLerutdi9Tn95GMZ6phdy5aIyJNN4h" +
            "YMcpHxcQJpO4TvhOSqmpj8A1XPv4CvOcbCQq/4kAso0X/98p6DfXRw2X/RLvutFRV3DseoKwe2vc1jgkgMHOGKINgPpDTTkN" +
            "0o+pMYSzlZqv82WQLrzygpxRiEQfgDHz841mByhCS9E6/awmiQf3cOyjdr64JEwrC3DLsCNC4Mh5b7jHFQNN929xbdPTep9a" +
            "n74iqlTOvV6QLJsJqntd0Kc3ILu+OXBbmp5FZBdqZ7pohMbbZul70lxuWwZGexE6oPRL1I1W002Loen946ckl22K8hh4V98E" +
            "g3HkuEairJW8SbmomQECRwZN2JTRVJPrZnBWj+zLUEiRb45NWUFYpuH1ySZ4NQlGdB8GAGZwH6OliZDQzTMN5XE8qlgLeapA" +
            "Ep1WI94MAgTvbrmn+9shi6C4gFdalfVYrqawiSzbVtemYvQcOxxQAF1/A3mMiN/vIorVb4b+awk9aNxDqNEFkJ6Joi2xkl1K" +
            "VZ9jyXvXzc50Brh5WF2EyrL3ICyydbhK7kOTyIfU05WiC5GZ+lDv4FmwDav4UaNkrRuOIgPFjWZobnCJP8+VvhMHrsT5e2s2" +
            "Izhd+tnjtDkeY6Z33EY6ZnZ1yjnpA3x1ColiCpVKpA8znHpxITh5sSpGo02kc7kyJfWB3eBjxHn3Pc/w4NjuyzkZJHkFECMA" +
            "FtV+KpnFHYO69VPE0a/NGhtOLZMQIIWAMCn1cY32qh4X4zfXVHxiIcPfLqR+43uTpYFFcC1UDmZcjWoJlFPBV+L/ytNjNnrT" +
            "yEJvoczWvBK/TtCjYiqxfBL0rchlwgyKKMuATPHBL+fqj3RF49RkXenwjLpHNfEuwWYeIqMMSq89xdGnVsb8iWuVtbgDTH3z" +
            "p2xM8iURfRCxG8UVwlbK68IwB1ZMSibceP1NQGz38lss9zxBidQq9pKJ9BADqGsbAefU0Bwj9Hn6Q+5M3ERJ5APByXZ03d3T" +
            "I753O1Y/HOBq1IqYwFHFpeFwA3hQcdE258iP3CEo4I9ab8OoyvQZxwh14YdTM4U8pTzulzBx3zgxyZ9ytvSkPoCNIzkGpRtt" +
            "FCR8AMiFLouUekvfcHIM4J3iFmEIs7RINoS0JxuaWHkLybZfI/Yr3y6s1EDxT/EPE3pddzP8Zv0eGF6MuxXMxpRAs81vaGv/" +
            "RXiKMhmE0gTMng3yhSjPzn14HXuhW2JcE4RWfLMz32C0edUB2V3rV/h5MniZw5I3EwJkqmru+WabzGtB92F3MGh6lGB1WkAA" +
            "/dnji563RVsqQmQq4/Rn4AiEwiU6ExLUsQ6bc9InpkvaIf+oH/b4XrPbVf36Dk8cAsGk+Z32heNnBR9spW8J4Z03jmj7DRKb" +
            "WAAfm4ayV8n+E2o6bbeNn34czjyMNJoTdUZAlXpoVHxikBfW2p/MAF8/SVvtAkpgJo9RJzo1eB0K1dTb1fkqvZlduyhHN6YT" +
            "QevzrDaUimPJdCJkK6Pc/LgwdynBai3+B5GgwYcc63l3LWbQYqSFjO4ZMn3W5CDHFiQeV8e9v3MHovfcmlWx/iDx3Ux3CSMQ" +
            "patKSYJSs0VaR106EUu0ogv9CReJ+K19Q+TDeL1Nf95137uUSwKK8nNn1r3lPlhg6OIxVbVAQj2R7UfbdM21HBkgGABYpbyo" +
            "g5yxievOHrATYZWKbwWIt2VoMVFSWZADxwFmJMzWD0YkqCDvHh4ENJ/3DyMScqXw2lgjA03iVJquhvhps4CHQ1YzeHE7yjr8" +
            "FzAW8/Xn+K/h/0cB/a/VdAfupMLDkOPDQXuL7XuyVOAqvePjvv75Po3yAMx/1R0Cs5MIz4f8alwLMsMGdMD/pdBYiKcenYar" +
            "6vWwzvW1gPSWGn68RosK6vU0BGNbr49PMktWrrh8lqhuEWhSx90T0jJlZnFL0dICzrLqf7tsOkNyEkljouw7aV04eiwsvOAZ" +
            "K8NUAOs5q8rG3tgz86mbtxXSxWAPR2aY1PuMlbh3v24YINVNAzQEhYIU6g22CSZXhxb2Xjv+rvyhaR3cACtRs8FhlwX0ZNLI" +
            "qbxuUHckRMYCW8fGURP+eyb4clwaa3pO9YzMqjdjcIvQQKi1KGqjBymORGH4qjsaCXKxBoQJXY8nz/ImSvphv8ygch60NmFx" +
            "FiYxX8XlsO81onmEW7wyd2NypAkvFp7cVjMRQhUZlTHe36EVZUc2ll7gJ5+xEt2sO/cs+D9NeLKj101eUYozyAddQAhC1+z+" +
            "s8sjy8IRZ0aFPQFSDuCNu/5XcSGt/I8EY1iBbrJJJ4Q6A2lgq7uUVd2ix7CrXbXytIfJ6EI9nf30DJJwMNEglypilxb1bk/r" +
            "A5IHG31ofhRWnk+0XPsDbDOUmJWCvkuoHyzhBR8SrfdKDrIPfHylhmECY1puQtRTRUGQYZIqwHthfq/fggXIx8QlJH1BJxEB" +
            "26zb/MNMtNktMFGdAwsBSQ+jS4f0utu7dtXWfNE7WWjils57D5KWyC5G4YsZvhEGhKD9z4sNfk0WcSxpKCZkGsPPZX3mFos7" +
            "9t5vzdEwvmE5FL/0//0Ig/eZ5GTJhJdJvAkFkTkd0mYKiIehGeF+jItai0hNRNndY2IR2LUInuHxKDukS1zsyX9+jaIaf0BP" +
            "shTd20fWX3HXYg3gNKtd5qQGGGbUead7rlOaeonCbI9wmWVNIiIKCE0OG6xnEyNyiAGXDf+YXEOii2ZUoala8cByNiJyXahI" +
            "Ql2cYrT8IPbE9Qo9+InP7wM/C4VUV/OPl1OqIwGRfXayYHOFKUecAoHuphPcJFVFijrHx/h2UpWJf5CZoYRZyW7w3IgvPkfy" +
            "xvFy+ZXC7Hj0BxmWuS7H8HTRN0nWyitwsL5tGEgs1mcheX9Nj+RvHFnKT1ndadfmLFgk9gIgtCa6buagOpUlHpjt988SFmTA" +
            "RudhMhgm2cWbFab3PdmENDDnAQPcnPcmx1R6af5ulf3DLqtHIESouVBgO34fZEjLaJHFT3yfMecDtQHUBfm4BhZvkyHik2Be" +
            "qRW8kkdeqj9cHOSrUgJcly+B4+qgklyIwmp8byM8dxG0gJws+lEtbxjDFddoAN9Zxcqt4kXDzHGhUDc3RrnPWA8XV+UiEJZg" +
            "eXpM4G0UnETOij5hnTSfw3W5qrEkxcZ0OH2XVkZnE84Wm4jUmBcS+8a6mbQHsAtyy8+H+QghvXVjN9WlS4GEcV3+R0StIE78" +
            "vxeQog3i/djfHZ08DbS2jsX/5KfeuatBmTSbCXWqRuadE5DKzdsUJ7SFCSJFnYgYNzqw0X3W4e2QpX5yOmsbaQ+6dHLSwlUM" +
            "hIjDoyPrjtwK12uhG85spqim+vlnaX8QCWdJmrOBNmwvjlxdQ6b2D7FSvb+LcOoi18/7XuJBvfj8o1voAoJ9m7Pgklm3zmLx" +
            "nsECUYzc6/IN2UVmbpWIPITh4+Hdjs7gbwriE+LfZvjPqylV2dwPu/FsxQa0yPaT2Ti5MOtkPd8qRadb/8JSfy0c0P2nU/zu" +
            "QWzwVYV5oX3/cWdHgh9cSbO2zigr9yP2q6TH2p6/NATOu2nQTceiyJG25trAmGH347xc1RHkMbBZ5665HHjtApyv2kDozcF5" +
            "HN56g3j3Vj3PtEU2qjPyKIPs/WjQJD3jpiFn1GuH/R3Eei/zlwAyANwZ410OWH/GZjFIbDPW/FWk2fCKGqCoCjDrJlLAHkLE" +
            "5aFm0hx7Qog/CtKfhnXNZDOQE+iGg+ZbEf+3eQgUOteNNcEWjCMWMOCdxbp8JMaSddb6JnAxgqbtYnDDikxHFb944NvgH3XM" +
            "YJP4gky+bk7HR8+WmAqgS6b5UkxYSZTm6ZCiZPKxCEVC5+ZqknYKL8R3RNSnbGjM2P89CUiCGgIg7v4AULQjcrogBqNw20Vz" +
            "eykM5fXzj3G5uF4nMlXzHC6aNgRIjbJ+Y0o96pUrU8pyOD3iVkIsKR3E1zeX8yaA9lkqLR41k344Sg2TZBbbVbLw+VTUeM0W" +
            "4EwuhKC1X3TsZDBsDMHGe1lgWtCivpJfNBIoGDtRJQM8LbkRV+Q8CQsPj7FiwWjeQF22PLMHUlZA+ZyR0ukqLgkzNEmxo/9o" +
            "5MN7DO7XYX9IGmG9oqraBOuTRcDB5TLwec8XKvDmbVw8DWBf4s2jvAoqAde8UfyCYCfUxXXMISaYRWdeoYqY74NoZZwuZo6Z" +
            "ZY/md9zOB1OrcENTuyHNdRdK0dgl2fN/P4aN9XLVKZoCEAvooiOHr68Y+PhYnPykUFHaZJGXCRfJlMI2JWi3tvLAWQ5NMlGA" +
            "w2mdehwzru56xvFbm0+oIFpBYcDuJcOPqZkMMdLgbyuXNXfm9UBXc14/2ovjV2V+fsXZ6ot6ZAh55zbYtbfXUJqHcbpQhR5A" +
            "f7d/Kt1M/0gKuW4zZh2i8u0RqIX31aNQnp+2OyyqOQupIpwkTANbkrHo1U17SFOd49GiUUGB2FmZnhzQZTk40QEC0+TTisHs" +
            "LGTGtvNPQLmFyp6RiC5ly3QaaeFQojQtGHQejC2h7YZpmWr+piYC3wBHN2rgG/gNtiHzQOGPB2tqQYJpcpJAK9Ja9nj7hTCr" +
            "CLiRn0dFdpd1jIMxf6kMqEJ0WyWbThzclK+12+V01uEdX2shjVUymAANubNEzd7goTCKnYJXIZSKYODqx4GeiUj1uXq5RwoU" +
            "ZUFfXvcaiXEtQmVF++o5Y9GOOEdSjYq49NK11q7rCJzPDmIodO0qXPJjQ2RBatQKV9CHzgBy8nkPVxd9Sk5cL1Hvas/ppivw" +
            "ciNR4jF2/OP6mKe0r8U+4Rj8f1g0a4FMzU80og0I0fI4aUrT1yeQCxVOoBxAlj7DHK/yFaWB1orfhnNSNuxmpNwbxDv7PJPd" +
            "NUG107LL5irSzKVEYJokcbnlTQOIE5gJZdvd9CELfHnjCv8hAD895gb81q0+t5O5fDr/2RHP7g3I3LGFGhQMSWJjmzba3jSD" +
            "BOaXXF/srOY62uxcZLcN5H4AJzBQi8/nU826xN+++ZXIcEHDuuYOQkaeQ9LBTLd3BE31ROYPSc79dpjOvDT/pVSY0AEFnv09" +
            "kzaayVufrO+6xKhstpIGbnads5QU7ONzYHomrVgM5qtz84s5Jf8YNgkc+d4VbfXn9h9/gzcCdo81IcKaLi4o7aYiiSUF0i5A" +
            "k4yaQKMpXHbuK60BAsIJ3g0MHl+1bpfqyd59bI088KIvsbGR7tLLCxPrNXROjf71TfdTCR9o6iaN9ELX5KgywP002s+EAsfI" +
            "A7zIXLWDnLtNHuFMOjYVysrr4TECUDujtf5u1pdJfgL6XU6/RpCSVyxnzNoOGaA4Nx4a1pMakg7YXulu3gO1NLUrXvqVlBD8" +
            "oLKhgRkQXSiuXMiyGgildlbbLfUdeLjZhGwhxAX7GosLWJGN0fXYP2mtAfOOUN87l+xXBsewgGAyhloEYoL9tFUuxJj1oOTa" +
            "9p1O74bqEtS5FgV52Uy90IDX+BhXZbCswS4xfhnD5gnMQn8xX/mHTC8BKPVo52/XQ7eVZQNR7oqbrCp+Jg8X03twkxsM895p" +
            "Im9XHXlUc7PCncecEki9FQsFZ/aa6muAEGGdN7QLLsM8eoMRqm4B4I1mPEadK4mxdfp+xfzNyBCr8rZY3opJtW/OP/OUAEOH" +
            "ZLuoggSLgtghI9N7hMT62/tHQWKBehpl3kSBRKsFwgJFj4LrQd97diitsnh3ttPHT6+mnZRhCyTRxaGrlePXguAwmdovsLeg" +
            "+e1cmb1gpKDblEwp1daFuC7yqgWFlmGjkTMiY3YVGiKBm60NjcIFxM522ptdCwZc9vXOYpcNjCYD8vvHbrVwREfY+M2sT25M" +
            "/DgPT5lrHygbJ0CoPPQnXEDIW9AHqjfOrKepXIFOQQcc8DWlVSNJYAIyYjnVQXR8wFRi47tbqsbY/OTfFaPxiGnaYwXpZmqi" +
            "ypgHHrR4QNElzMHyeK4DBFLTrVIp2GrMZ4cK5o4veic3cvjc1iDHn3S9PdRficjlpLCPVhx1cpbUxY2sNjTONGq22FBsQlCn" +
            "AAYHaPuUEQAwLbgVNcUR2VVFEiCJTeDSrpzxjhNPiDGCgNfOkj1GoX4TYyIT+QgMcoObaqy5fChrOlpINWdtQqVCL4mfBXZE" +
            "jQfj7qWOuaml6AxBKQxVG0gIWZfUS723ohFkx7lV5xcJZnfUTHYWc9eqSJOOISt8kWqiMJHvV8ONTGbFFHTmZ0aBfVzKCfD5" +
            "LTNjj7xZWrYNx/3R0PlfWZaPnXxB1cP9OgjAQ9JUTVDk3RxfLICAEcyDs381sCCTV5r7TtAAriw3Yi7sLNdcRK3TG0iNhP3x" +
            "uyJd6AAEl8W0IPZN5T/LZVrNVLZFaGP3IhwqbWEflDJmREPWr3hWI4A8CH7uuCNBVzddrVqRUxIredKdMJ5dpYMr5lLoeggd" +
            "MJSOGXkDs9AguFBSZpicdC7G+uWZvHpWOVtUudpjlBk0CQfFNMGb+3YWI5ERXrmGMg8E3WpvhyrQvscDfKnfjDOWSnbv6R+7" +
            "Tifo/7nkScsoS7iNMUyX4DR+iXC/QignPpjy3um1LTXWnLFMzVSC1RiQhB/5U4/8n+EdBVtY/34K9Gn3kN3LjIoLXgcoJFii" +
            "tBW7WXGmYfjWUBoTNIlhBlFibtZ7sLpQDYu1Gi89PCs6FAR6YvJqJB8KRp9obVc3ROQOtrMYC1pNNUg/rvPM7+SsURv8uu0/" +
            "j8q8Xh4cLCZX8SwqoV8+y+eIg2NwtiFGS6kA8S6WTsLIQOYzaD5yDXBNoctoXnExfCD1GKVsdCRO5Z4MokyRqyGNouvmejtC" +
            "/uHj0sGq89U6PInPUNZStMb7gNkYNzffFBfdNtaDRUumwXvtIjHnDY6a30h+6pBJjY4Vwz06JeGOw0sIvrjuuIXjEFRAZEfV" +
            "dWFgmVlJStHvEHTMlUzY+0Kukc+zinhetQY8H4BSKTH32GU2vJJrjztNUqRvh0IFLIWXRJJ+79F9fwZUGpmS+EeZGBtsRBwo" +
            "Dr+vDl9+4ChVmhn1zCj7i5avRLdPRYNKmCv3FLUiLoIRAvOk9bvn7P17IEe9SlaSEifod5KvFDeOXSa5sNVisPs5suzsprNk" +
            "47fCG7Loc6KwZbiitEuSdWfNSeiJkNmd739iaJRSO4tt7FuowLcyGDvPj6zpx8PSssmFPmMKUQzG2ARpCmdL59bxvjAVICTa" +
            "tFg3r7tquDVyF861rjOSvZkggctKQrVOKG1SioWILjGNM2aYw3FN5dWiOGW1dzTNFDas26xlvmEUWyvcZ/MTE47btxBkT0jr" +
            "lEm1vmEUZ0M4wAapQlAU78XQQrMBeXa0BRlrzu5Tcv7+8hS6OlIHjsKMGrq/uUapToLHvOQO1sPIoBpFUH9+tsr1tO07FNgV" +
            "ec0apYAVza2JI6DFDRDFPC43vOj1tky5CnSFStGcv6TnBAkoc+c4pUmBsCUYJRxrv2tZJWevXLJApkucBgeTUB40blrNPKjf" +
            "FJXM6A4jfc7xEyLNWQ3PPbiHpBqmicgYjM1zrchwfuuYknavgrRtvY2sZWJPraDv21goSMyqA1kVptBIguutLuzJ79m6y1Je" +
            "evw1mgNvSpK8zqkeRagn5uevKVACD10+URs2H52eQM0Tz/1sHg8CQs9NtAUkl94srMMhEvQ4hd4cJqwdxWI7ErvG1JUPSPdJ" +
            "9FUId2so0QQmMI0f5jrEK9G3VqJ1BWDRdAzJ9gMdXQZC/fCQzzDtZ0PpIXYtzAcxSr/r4Ovg+VTuNn3BWV49EbvmK2zWG/XI" +
            "MH0/JSOWgG0/ttYEutjuG+EwfTay8pzpln6CQbGOsh7Mb9yJgrnQF2ugey6H2nYp44S3rXFSz+ZXBpX2VSLd4ERd+qBj9aaY" +
            "4EupCfBaOAucowdNxlKqBt+lauRn/jGwvWioRDDRO+Ql/Lttqxntl+KmGOCkXRNn0qy0SwC8p6gjlRhWLzDK+QhEtVaw+iXl" +
            "eoQ0vKQ47DcFq/xyW20I9DIy5m//POD4vQgUb73hTXkTcvER/DBhrOrNiqaTR4MWrjavIPRfkAaNzrTAC6WEx8xpymIgV0Xa" +
            "f3Rhr6Ze60cowH7suM9ftw1zBg7REKiCowbCyLcP4vS5ktnkZVg8DCVxzZ5e4ldiNJ7eDZDC3QhbOe+U/Bt5AZFOodGzMQSr" +
            "9QV1asov+GaIZFwiTVgGaYyEG4pdc9c2/5dP1kZAzpGCMbEAR/hKkRqCR5oDB29MgVX76IDEpxX8coqMDyz9gv2Znny25Hix" +
            "P+vzbv+dk5pO9WoiJSfGArP3bPmHCWpSDgbfm24fx1IirTpRzUl6mRfZqDiiczgajUinORv46Nd18xrlE7BUb4/CckiZc/hN" +
            "IvumQ6p8wjjjnWmgoz67bzjkqjd8mWXUIS7KMUf0UCa6+cGEGQr2Z0J18cldOI+afzxxH2uBk7H7QUaO5LXD/ecvTzflsTJX" +
            "zyYDSb335LlY6VImuxv/eHhK9l+h6TLg++BPARYZ+QQX4qe8Ag9odDHsbvQjAyh73mfKjDUDHWwo4exHR/ReUQN2lGOanqfE" +
            "wUTZamQnFGqxgZrDKbOx3IpCOyY2bMlkpMoLBKpIksL6ABGpCqAu6+4MI71JfzruRwBnCcH1wl7q1oFY73Llj+6caQ9IFNmw" +
            "NAyj2SVOxZWrJoR9UiFjvw4w+ByMwifiHGY7srR/CW+2COYbGIFA0IY6qixKg71gyiqdEye6sWVv8As0/mQUF0yhPVCFZLWh" +
            "mCS+IR9eaUiNxeWi1sFnYcwsypSE5hNFnBzhlrhkXiWH4yRIg9LW7MM1gw+Ye40JUF+6cvn2RQlqTSujO5rV58GAqBH8xeRR" +
            "twB+vKzsplRJl5LXbSQlg/p6WhwegmwcWBi8IErIyYoFGMPihJvr+2wWcT42piGwtor82lS41nZM/kFGktdXLeK11m9kz0q9" +
            "HTjcVyW8NyPZrHh8hhdQ2otffrXUDv6WC/WyYmp9ccDz7qigsni110jz9QloxQGOnrJThMqM++yRhgag6EtqqrnmkNIV4OSY" +
            "R3kFuIBgFXyJEJXT16n4DPHg3sWPc1DR0q8zy74eR+YItv1hojR03QDguiOB3sWa+r/OXDlVkbitFGIpNTxIMs6da2QQVuu5" +
            "1phLObLQorOXuvlwwf20wjEIkVFJaNs4hLUD/cHQ46dAM0CzPKNqHHE8y2m7NRcOaRGHuJXtZudwCvqIUzKB548tTAr2TuOF" +
            "RruTedOsS5n65jquvVSW0CiNR696sc49o5D+IaqYQXpGPDlFWrULgRl9gepGdUUNpYJfE62KJRAAV3Iad5CAFRATFCumg2z0" +
            "DXMqJITxn0eKNQsu/EIEcbGfsyqsDjo2+1NcBl/Vn7cFPrjEkxVitx8VY77I46IJg0/DKX7Sb+EXAll1uWT62cIrzxqnRWgf" +
            "mDtLW3nh8DNb6YjIVD/is/lkzLxWbNoYJCu7+JPSRn5D+f02nw6M51uWHxnspr9wFTynnRxAoLcyG4TCcNH9dFgmiNSKXizR" +
            "LQu9AVSdf9CRnpDlal+Poj2SNEbEEPnXq63+6k7ZPJyHuWO/GbqulAsMY/7QIZnCa+1IVLihpSbYSdFchOKJprWbNKe2tzkf" +
            "gKub9+7MBCFcYqWUd1P7nWRCFfsGVc5zW10Z5Az/SPpmQKiG6U/HGmUsssVUq5jCiN3jUJ873UdVQJo8v7KeJYGhnR+I1Lh3" +
            "RXjQsRfyNyk47VAPzvR5q4/Hs2O5ubDU2VAoqv6/nMooiWaMBAwTccVfwYwppT2AYsdpV++A9ODyXPjtoTJpatNLLVfDrtAG" +
            "OiEJG+ifXFcJWssI5lJdG/Z75OQh/QRoM1jB52yJPw/14Fzl/I7vxoit/+GqkZxrigP/bE4zPAlQUJO4HaW9lDNSvNEiQmzy" +
            "jUqFvcrVcf6aXBlyAAJadX6OcsUZWwkA42vd0Lo1u1kXoQNWuY8w76CZfsQeJndc0HuECthGVhHGhpJQS931arzAGMAnkQ/3" +
            "2YuRr3b1tCjD8nos6ltHfVYY0zTVsgNJw2eiel0Rkee4kei4eAM9bnt6z4gTQ0ejOe3knxtIVb0oie/Ou0bn0G5Ox6NNMUGh" +
            "HFGgI4aRuuj/oUE87Ck4ST+29AWd02uz582vQjFJFczewxwBq+BZDz5myRUmsD8uFs0JdOHnXlNXumzyzXcvUVFkUdr3DaXX" +
            "Fmi7gk+lMRhtRO+T/FWmNv2amobyP1Z8PHBy8V7MXENNnffkZIkFEMZAmGcDvDWthRIhdgAOysg/0UIZeExnG2l40AEP98bP" +
            "Yg8pR4uF4eAOuyyo/KRBndEgxEymKVDPE+Bw20KMOKUp+0F9TCHlDgkjj8bkZdWAnhYrGh2qd5D5xmgnrmLKmKoc8G+kDAhf" +
            "54yuDYqFDQzOmYn+laKnYlFuwiEmLeGV689fKJN2W+vHwKfm0NK5AaiP+BexEnFGozDolwbU6RezaMCvfSg+XN4hQi9xDF0/" +
            "mYPYqQRyCzT8gdc9hOMWcg4LEML9zhUPJZLGtwz1IPv5k9Idv3q5xXCoELr9FYrMYhZKYITALLYVkNIc0TvhoRWLFeN4+zfn" +
            "WHtFbI52+qfXlDpgNYp5zVffNK3ye3DlCrPf1fcTUXYf6W+E+Q==";



    public final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();
    private WindowManager wm;
    private int screenW, screenH;
    private SharedPreferences sp;

    private ImageView pet;
    private WindowManager.LayoutParams petLP;
    private TextView bubble;
    private WindowManager.LayoutParams bubbleLP;

    private int[] cruiseF = new int[5], happyF = new int[5], sadF = new int[5], workF = new int[5];
    private int frameIdx = 0, emoIdx = 0, emoLoops = 0;
    private boolean playingEmo, dead;
    private String state = "cruise";
    private float px, py, vx = 2f, vy = 1.5f;
    private int tapCount = 0;
    private long lastTapAt = 0, downAt = 0;
    private float downX, downY;
    private boolean dragged, petted;
    public boolean dnd = false;
    private boolean pending;
    private String updateUrl = null;

    public static PetService instance;
    public volatile float speedMul = 1f;
    private int deadRes;
    public String apiBase, apiKey, apiModel;
    public boolean workMode = false;
    public final java.util.Map<String, java.util.List<String>> quotes = new java.util.HashMap<String, java.util.List<String>>();
    private long firstAt = 0;
    private final java.util.List<String> chatLog = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
    public int affection;
    private long lastBubbleAt = 0;

    // ---------------- 阶段1新增：状态机/情绪/饲养 ----------------
    public final Emotion emo = new Emotion();
    public Flyer fly;
    public float petScale = 1f;
    private long startedAt = 0;
    private long lastInteractAt = 0;
    private long lastPetAt = 0;
    private long lastAiAt = 0;
    private boolean napping = false;
    private boolean afkWarned = false;
    private String lastDominant = null;
    private float pinchStartDist = 0;
    private int feedPhase = 0;          // 献血三段式：0无 1俯冲 2盘旋 3叮咬
    private long feedAt = 0;
    private float feedX = 0, feedY = 0;

    // 甩动惯性：拖动速度采样
    private float dragVx = 0, dragVy = 0;
    private float lastMoveX = 0, lastMoveY = 0;
    private long lastMoveAt = 0;

    // 时间感知：时段名+跨时段问候
    private String lastPeriod = null;

    // 屏幕边缘小拉手 + 悬浮聊天框
    private TextView tabHandle;
    private WindowManager.LayoutParams tabLP;
    private LinearLayout chatPanel;
    private WindowManager.LayoutParams chatLP;
    private EditText chatIn;
    private TextView chatStatus;

    // ---------------- 阶段5：整蛊模式 ----------------
    public PrankEngine prank = null;
    public boolean prankMode = false;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashActivity.install(this);
        sp = getSharedPreferences("weng", MODE_PRIVATE);
        instance = this;
        firstAt = sp.getLong("first", 0);
        if (firstAt == 0) {
            firstAt = System.currentTimeMillis();
            sp.edit().putLong("first", firstAt).apply();
        }
        affection = sp.getInt("aff", 0);
        speedMul = sp.getFloat("speedMul", 1f);
        initApi();
        loadQuotes();
        DataStore.init(this);
        emo.load();
        petScale = DataStore.getFloat("petScale", 1f);
        fly = new Flyer(this);
        startedAt = System.currentTimeMillis();
        lastInteractAt = startedAt;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;
        loadFrames();
        startForeground(1, buildNotification());
        createPet();
        createBubble();
        createTabHandle();
        randomizeVelocity();
        handler.post(tickMove);
        handler.post(tickFrame);
        handler.postDelayed(this::metaTick, 30000);
        handler.postDelayed(this::moodTick, 60000);
        handler.postDelayed(this::autoStateTick, 12000);
        handler.postDelayed(this::afkTick, 30000);
        handler.postDelayed(this::reminderTick, 60000);   // 喝水/久坐/DDL/整点报时
        handler.postDelayed(this::weatherTick, 3000);     // 每日天气播报（3s 后首查）
        handler.postDelayed(this::idleChatTick, 90000);   // 主动搭话
        handler.postDelayed(this::appSenseTick, 25000);   // 前台应用感知/会议勿扰
        handler.postDelayed(this::extrasTick, 180000);     // 随机事件/小游戏/小剧场
        handler.postDelayed(this::achieveTick, 120000);    // 成就检查
        if (!DataStore.getBool("tutorialDone", false)) {
            handler.postDelayed(this::playTutorial, 1500); // 首次教程
        } else {
            handler.postDelayed(this::firstGreeting, 800);
        }
        handler.postDelayed(this::checkUpdate, 5000);
    }

    // ---------------- 版本检查 + 热更新 ----------------

    public String curVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    private static int verCmp(String a, String b) {
        try {
            String[] sa = a.replace("v", "").split("\\.");
            String[] sb = b.replace("v", "").split("\\.");
            int len = Math.max(sa.length, sb.length);
            for (int i = 0; i < len; i++) {
                int x = i < sa.length ? Integer.parseInt(sa[i].trim()) : 0;
                int y = i < sb.length ? Integer.parseInt(sb[i].trim()) : 0;
                if (x != y) return x - y;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /** 查询 GitHub 最新 Release；比当前新则下载 APK，完成后自动拉起安装器 */
    public void checkUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(
                        "https://api.github.com/repos/coldpaper0953/wwwww/releases/latest").openConnection();
                c.setRequestProperty("Accept", "application/vnd.github+json");
                c.setConnectTimeout(15000);
                c.setReadTimeout(20000);
                InputStream is = c.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
                is.close();
                JSONObject j = new JSONObject(bos.toString("UTF-8"));
                final String tag = j.optString("tag_name", "");
                String assetUrl = null;
                org.json.JSONArray assets = j.optJSONArray("assets");
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject a = assets.getJSONObject(i);
                        if (a.optString("name", "").endsWith(".apk")) {
                            assetUrl = a.optString("browser_download_url");
                            break;
                        }
                    }
                }
                if (tag.isEmpty() || assetUrl == null) return;
                if (verCmp(tag, curVersion()) <= 0) return;
                handler.post(() -> showBubble("发现新版本 " + tag + "！正在下载…", 8000));
                downloadAndInstall(assetUrl);
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void downloadAndInstall(String url) {
        new Thread(() -> {
            try {
                File dir = getExternalFilesDir(null);
                if (dir == null) dir = getFilesDir();
                File out = new File(dir, "update.apk");
                if (out.exists()) out.delete();
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(20000);
                c.setReadTimeout(120000);
                InputStream is = c.getInputStream();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
                byte[] buf = new byte[16384];
                int n;
                long total = 0;
                while ((n = is.read(buf)) > 0) {
                    fos.write(buf, 0, n);
                    total += n;
                }
                fos.flush();
                fos.close();
                is.close();
                final long ftotal = total;
                handler.post(() -> {
                    showBubble("新版本下载完成（" + (ftotal / 1024) + "KB），拉起安装～", 5000);
                    installUpdate();
                });
            } catch (Exception e) {
                handler.post(() -> showBubble("更新下载失败，稍后再试", 3000));
            }
        }).start();
    }

    private void installUpdate() {
        try {
            android.net.Uri uri = android.net.Uri.parse("content://com.weng.weng.updatefiles/update.apk");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            showBubble("安装未启动：请在系统设置里允许本应用安装", 4000);
        }
    }

    private Notification buildNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel("pet", "嗡嗡嗡", NotificationManager.IMPORTANCE_MIN);
        nm.createNotificationChannel(ch);
        Notification.Builder b = android.os.Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, "pet")
                : new Notification.Builder(this);
        android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, 0,
                new Intent(this, SettingsActivity.class), android.app.PendingIntent.FLAG_IMMUTABLE);
        return b.setContentTitle("嗡嗡嗡在飞").setContentText("点这里打开设置")
                .setContentIntent(pi)
                .setSmallIcon(android.R.drawable.sym_def_app_icon).build();
    }


    /** 解密内嵌人设（与电脑版同一套 W1 方案：SHA-256(key||counter) CTR + base64） */
    private static String persona() {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (String s : SEED) sb.append(s);
            byte[] key = md.digest(sb.toString().getBytes("UTF-8"));
            String[] parts = BLOB.split(":");
            long n = Long.parseLong(parts[1], 16);
            byte[] ct = android.util.Base64.decode(parts[2], android.util.Base64.DEFAULT);
            byte[] ks = new byte[ct.length];
            int made = 0;
            long c = n;
            while (made < ct.length) {
                md.reset();
                md.update(key);
                md.update((byte) (c >> 24));
                md.update((byte) (c >> 16));
                md.update((byte) (c >> 8));
                md.update((byte) c);
                byte[] block = md.digest();
                int copyN = Math.min(32, ct.length - made);
                System.arraycopy(block, 0, ks, made, copyN);
                made += copyN;
                c++;
            }
            byte[] pt = new byte[ct.length];
            for (int i = 0; i < ct.length; i++) pt[i] = (byte) (ct[i] ^ ks[i]);
            return new String(pt, "UTF-8");
        } catch (Exception e) {
            return "嗡嗡~";
        }
    }

    private void loadFrames() {
        for (int i = 1; i <= 5; i++) {
            cruiseF[i - 1] = getResources().getIdentifier("mosquito_" + i, "drawable", getPackageName());
            happyF[i - 1] = getResources().getIdentifier("happy_" + i, "drawable", getPackageName());
            sadF[i - 1] = getResources().getIdentifier("sad_" + i, "drawable", getPackageName());
            workF[i - 1] = getResources().getIdentifier("work_" + i, "drawable", getPackageName());
        }
        deadRes = getResources().getIdentifier("dead", "drawable", getPackageName());
    }

    // ---------------- 宠物本体 ----------------

    private void createPet() {
        pet = new ImageView(this);
        pet.setImageResource(cruiseF[0]);
        petLP = new WindowManager.LayoutParams(PET_SIZE, PET_SIZE,
                overlayType(),
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
                    wakeUpIfNapping();
                    downX = ev.getRawX();
                    downY = ev.getRawY();
                    downAt = System.currentTimeMillis();
                    dragged = false;
                    petted = false;
                    pinchStartDist = 0;
                    handler.postDelayed(() -> {
                        if (!dragged && System.currentTimeMillis() - downAt >= 650 && !dead) {
                            petted = true;
                            petHead();
                        }
                    }, 660);
                    handler.postDelayed(() -> {
                        if (!dragged && !petted && System.currentTimeMillis() - downAt >= 1550 && !dead) {
                            petted = true;
                            openSettings();
                            showBubble("打开设置啦～", 1200);
                        }
                    }, 1560);
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    // 第二根手指按下：进入捏合缩放模式
                    if (ev.getPointerCount() == 2) {
                        pinchStartDist = fingerDist(ev);
                        dragged = true;   // 捏合不算拖拽/手势
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (ev.getPointerCount() == 2 && pinchStartDist > 0) {
                        float d = fingerDist(ev);
                        petScale = Math.max(0.33f, Math.min(2.67f, petScale * (d / pinchStartDist)));
                        pinchStartDist = d;
                        applyPetSize();
                        return true;
                    }
                    float mx = ev.getRawX(), my = ev.getRawY();
                    if (Math.hypot(mx - downX, my - downY) > 18) {
                        dragged = true;
                        // 速度采样（松手惯性用）：记最近一次 MOVE 的位移/时间
                        long nowT = System.currentTimeMillis();
                        if (nowT > lastMoveAt) {
                            dragVx = (mx - lastMoveX) / (nowT - lastMoveAt) * 16f;   // 换算成每帧(30ms的一半)位移的量级
                            dragVy = (my - lastMoveY) / (nowT - lastMoveAt) * 16f;
                        }
                        lastMoveX = mx;
                        lastMoveY = my;
                        lastMoveAt = nowT;
                        px = mx - curSize() / 2f;
                        py = my - curSize() / 2f;
                        clampPet();
                        petLP.x = (int) px;
                        petLP.y = (int) py;
                        wm.updateViewLayout(pet, petLP);
                        hideBubbleFollow();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (pinchStartDist > 0) {   // 捏合结束：存尺寸
                        DataStore.putFloat("petScale", petScale);
                        pinchStartDist = 0;
                        return true;
                    }
                    float ux = ev.getRawX(), uy = ev.getRawY();
                    long dur = System.currentTimeMillis() - downAt;
                    float dist = (float) Math.hypot(ux - downX, uy - downY);
                    if (petted || dead || dragged) {
                        if (dragged) {
                            // 甩动惯性：快甩→扔出坠落；普通拖放→按松手速度滑行衰减
                            float speed = (float) Math.hypot(dragVx, dragVy);
                            if (dist > 60 && speed > 18f) {          // 快甩：扔出
                                state = "falling";
                                emo.add("生气", 8);
                                showBubble(q("throw"), 1500);
                            } else if (speed > 1.2f) {                // 惯性滑行
                                fly.glide(dragVx, dragVy);
                                if (speed > 8f) showBubble(Quotes.pick("glide", rnd), 1500);
                            } else {
                                fly.switchTo("cruise", 0);            // 慢放：原地恢复巡航
                            }
                        }
                        lastInteractAt = System.currentTimeMillis();
                        return true;
                    }
                    lastInteractAt = System.currentTimeMillis();
                    if (dist > 60) {                      // 扔出去
                        state = "falling";
                        emo.add("生气", 8);
                        showBubble(q("throw"), 1500);
                    } else if (dur < 350) {               // 戳
                        long now = System.currentTimeMillis();
                        tapCount = (now - lastTapAt < 1200) ? tapCount + 1 : 1;
                        lastTapAt = now;
                        if (tapCount >= 3) {              // 三连拍扁
                            tapCount = 0;
                            smackDead();
                        } else {
                            playEmo(happyF, 1);
                            emo.add("开心", 6);
                            showBubble(tapCount == 1 ? "嗯？" : "别闹…", 1800);
                            awardAff(1);
                            aiChat(tapCount == 1 ? "用户戳了你一下" : "用户又戳了你一下，这是第 " + tapCount + " 次");
                        }
                    } else if (dur >= 1500) {             // 长按拥抱
                        fly.switchTo("sleepy", 300);
                        playEmo(happyF, 2);
                        emo.add("开心", 10);
                        awardAff(3);
                        aiChat("用户长按着你抱了很久，你假装不在意但其实快睡着了");
                    } else if (dur >= 500) {              // 温柔摸（0.5-1.5s 无位移松手）
                        long now = System.currentTimeMillis();
                        if (now - lastPetAt > 10000) {
                            lastPetAt = now;
                            awardAff(1);
                        }
                        emo.add("开心", 5);
                        aiChat("用户温柔地摸了你一会儿");
                    }
                    return true;
            }
            return false;
        });
    }

    /** 两指间距（捏合缩放用） */
    private float fingerDist(MotionEvent ev) {
        float dx = ev.getX(0) - ev.getX(1);
        float dy = ev.getY(0) - ev.getY(1);
        return (float) Math.hypot(dx, dy);
    }

    public int curSize() { return (int) (PET_SIZE * petScale); }

    /** 设置页滑块调大小（px 32-256） */
    public void setSizeByPx(int px) {
        petScale = Math.max(0.33f, Math.min(2.67f, px / 96f));
        applyPetSize();
    }

    public void saveSizeNow() {
        DataStore.putFloat("petScale", petScale);
    }

    private void applyPetSize() {
        petLP.width = curSize();
        petLP.height = curSize();
        try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
    }

    /** 好感度入账（每日 50 上限），返回 [实际增加, 是否被截断] */
    public int[] awardAff(int n) {
        int prev = DataStore.getAff();
        int[] r = DataStore.addAff(n);
        affection = DataStore.getAff();
        int ms = DataStore.milestoneCrossed(prev, affection);
        if (ms > 0) {
            showBubble(Quotes.pick("milestone", rnd, String.valueOf(ms), DataStore.titleFor(affection), null), 5000);
            DataStore.setPendingTheater("aff" + ms);
            aiChat("你和蚊子的好感度刚刚突破 " + ms + "，跨入「" + DataStore.titleFor(affection) + "」阶段，它很感动");
        }
        if (r[1] == 1) showBubble("今天的好感已满～明天再来！", 2500);
        return r;
    }

    /** 献血入口（设置页调用）：血池为 0 拒绝；三段式叮咬演出 */
    public void startFeed() {
        if (dead || feedPhase != 0) return;
        if (DataStore.getBlood() <= 0.5f) {
            showBubble("血量还没攒够，等会儿再来～", 3000);
            return;
        }
        feedPhase = 1;
        feedAt = System.currentTimeMillis();
        feedX = px;
        feedY = py;
        fly.switchTo("dash", 30);
        showBubble("来咯来咯～", 1200);
    }

    private void feedTick() {
        if (feedPhase == 0) return;
        long t = System.currentTimeMillis() - feedAt;
        if (feedPhase == 1 && t > 600) {          // 俯冲完→盘旋
            feedPhase = 2;
            feedAt = System.currentTimeMillis();
            fly.switchTo("spiral", 270);
        } else if (feedPhase == 2 && t > 8000) {  // 盘旋完→叮咬
            feedPhase = 3;
            feedAt = System.currentTimeMillis();
            fly.switchTo("dash", 12);
        } else if (feedPhase == 3 && t > 400) {   // 叮咬结算
            feedPhase = 0;
            boolean picky = rnd.nextInt(100) < 15;
            boolean crit = rnd.nextInt(100) < 18;
            if (picky) {
                showBubble(pick("今天不想吸你的，想去外面觅食～", "哼，姿势不对，改天再来！"), 3500);
                aiChat("用户要给你献血但你今天挑食不想吸");
                return;
            }
            float blood = Math.min(DataStore.getBlood(), 20f);
            DataStore.setBlood(DataStore.getBlood() - blood);
            DataStore.addBloodTotal(blood);
            float gain = blood * (crit ? 1.0f : 0.5f);
            DataStore.setSatiety(Math.min(100f, DataStore.getSatiety() + gain));
            if (crit) {
                pet.setScaleX(1.45f);
                pet.setScaleY(1.45f);
                showBubble("✨这血也太新鲜了！！", 4000);
                handler.postDelayed(() -> { pet.setScaleX(1f); pet.setScaleY(1f); }, 1500);
            } else {
                pet.setScaleX(1.3f);
                pet.setScaleY(1.3f);
                handler.postDelayed(() -> { pet.setScaleX(1f); pet.setScaleY(1f); }, 1800);
            }
            String oldT = DataStore.bloodTitle();
            showBubble("吸了 " + (int) blood + " 血，饱食度 +" + (int) gain + (crit ? "（暴击！）" : ""), 4000);
            aiChat(crit
                    ? "用户献血给你，这次血超新鲜，你暴击吸了双倍"
                    : "用户献血给你，你吸了一口");
            // 跨献血称号档
            float total = DataStore.getBloodTotal();
            float[] lines = {250, 600, 1500, 3000};
            for (float l : lines) {
                if (total >= l && total - blood < l) {
                    showBubble("🎖️ 献血称号晋升：" + DataStore.bloodTitle(), 5000);
                }
            }
            // 吃撑检测
            if (DataStore.getSatiety() > 90) {
                DataStore.setStuffedUntil(System.currentTimeMillis() + 150000L);
            }
        }
    }

    private void clampPet() {
        int sz = curSize();
        if (px < 0) { px = 0; vx = Math.abs(vx); }
        if (px > screenW - sz) { px = screenW - sz; vx = -Math.abs(vx); }
        if (py < 40) { py = 40; vy = Math.abs(vy); }
        if (py > screenH - sz - 60) { py = screenH - sz - 60; vy = -Math.abs(vy); }
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
            feedTick();
            if (napping) return;
            if (workMode) return;   // 工作模式悬停不动
            if (dnd) {              // 勿扰：安静沿边巡逻
                if (!fly.state.equals("edge_walk")) fly.switchTo("edge_walk", 0);
                fly.ax = fly.ax == 0 && fly.lastSwitchAt == 0 ? 0 : fly.ax;
                fly.step();
                petLP.x = (int) px;
                petLP.y = (int) py;
                try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
                return;
            }
            if (state.equals("falling")) {
                py += 18;
                if (py >= screenH - curSize() - 60) {
                    py = screenH - curSize() - 60;
                    state = "cruise";
                    fly.switchTo("dizzy", 60);
                    handler.postDelayed(() -> aiChat("用户刚才把你狠狠甩了出去，你摔得头晕眼花"), 300);
                }
                petLP.y = (int) py;
                try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
                return;
            }
            // 常规：Flyer 状态机驱动（falling 由上面处理；flyer 的 cruise 内含撞边反弹）
            if (state.equals("falling")) return;
            fly.step();
            // 状态同步（snapping 等剧情态存 state，运动态存 fly.state）
            if (!state.equals("snapping") && !state.equals("stunned")) state = fly.state;
            petLP.x = (int) px;
            petLP.y = (int) py;
            try { wm.updateViewLayout(pet, petLP); } catch (Exception ignored) {}
            if (bubble.getVisibility() == View.VISIBLE) placeBubble();
        }
    };

    private final Runnable tickFrame = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 100);
            if (dead) {
                pet.setImageResource(deadRes != 0 ? deadRes : cruiseF[0]);
                pet.setColorFilter(null);
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
            if (workMode) {
                frameIdx = (frameIdx + 1) % 5;
                pet.setImageResource(workF[frameIdx]);
                return;
            }
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
        emo.add("生气", 15);
        Emotion.record(emo.dominant());
        hideBubble();
        showBubble("你把我拍扁了！！！", 1500);
        handler.postDelayed(() -> {
            dead = false;
            playEmo(sadF, 2);
            emo.add("孤独", 8);
            showBubble(q("revive"), 2500);
            aiChat("用户快速连点三下把你拍扁了，你复活后很委屈");
        }, 2000);
    }

    private void petHead() {
        playEmo(happyF, 1);
        emo.add("开心", 10);
        long now = System.currentTimeMillis();
        if (now - lastPetAt > 10000) {
            lastPetAt = now;
            awardAff(1);
        }
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
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        bubbleLP.gravity = Gravity.TOP | Gravity.START;
        wm.addView(bubble, bubbleLP);
    }

    public void showBubble(String text, long ms) {
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


    private static int overlayType() {
        return android.os.Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

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

    // ---------------- 阶段1新增：周期任务 ----------------

    /** 30s：饱食度下降/血池回复/吃撑结束/打嗝/饥饿催血 */
    private void metaTick() {
        handler.postDelayed(this::metaTick, 30000);
        float sat = DataStore.getSatiety();
        float blood = DataStore.getBlood();
        boolean night = isNight();
        // 饱食度：夜间掉 1.6 倍
        sat -= night ? 0.09f : 0.055f;
        // 血池被动回复：夜间 0.8 倍
        blood += night ? 0.05f : 0.065f;
        DataStore.setSatiety(sat);
        DataStore.setBlood(Math.min(100f, blood));
        // 吃撑打嗝
        if (System.currentTimeMillis() < DataStore.getStuffedUntil() && rnd.nextInt(100) < 25) {
            showBubble(Quotes.pick("stuffed", rnd), 2000);
            if (fly.state.equals("cruise")) fly.switchTo("drift", 150);
        }
        // 饥饿催血
        if (sat < 35 && !dnd && !dead && rnd.nextInt(100) < 40) {
            showBubble(Quotes.pick("hungry", rnd), 3500);
            if (fly.state.equals("cruise")) fly.switchTo("sleepy", 200);
        }
        // 起床气/小睡结束
        if (napping && System.currentTimeMillis() > napUntil) {
            napping = false;
            showBubble("（揉眼睛）唔……睡饱了", 2500);
        }
        emo.save();
    }

    private long napUntil = 0;

    private boolean isNight() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= 22 || h < 7;
    }

    /** 夜间小睡：22:00-次日 7:00，启动 5 分钟后才有资格，随机 1-5 分钟 */
    private void maybeNap() {
        if (napping || dead || dnd || workMode) return;
        if (!isNight()) return;
        if (System.currentTimeMillis() - startedAt < 300000) return;
        if (rnd.nextInt(100) < 30) {
            napping = true;
            napUntil = System.currentTimeMillis() + (60000L * (1 + rnd.nextInt(5)));
            fly.switchTo("corner_rest", 100000);
            showBubble(q("sleep"), 4000);
        }
    }

    /** 点醒打盹中的蚊子→起床气 */
    private void wakeUpIfNapping() {
        if (napping) {
            napping = false;
            emo.add("生气", 12);
            Emotion.record("生气");
            showBubble("干嘛啦！人家正睡得香！", 3000);
            aiChat("用户把你从睡梦中戳醒，你起床气很重");
            fly.switchTo("dizzy", 50);
        }
    }

    /** 60s：情绪漂移+心情记录+主导切换播表情+时间感知跨时段问候 */
    private void moodTick() {
        handler.postDelayed(this::moodTick, 60000);
        long mins = (System.currentTimeMillis() - lastInteractAt) / 60000L;
        emo.drift(mins);
        timeSenseTick();
        String d = emo.dominant();
        if (d != null && !d.equals(lastDominant)) {
            lastDominant = d;
            Emotion.record(d);
            if (d.equals("开心")) playEmo(happyF, 1);
            else if (d.equals("生气") || d.equals("孤独")) playEmo(sadF, 1);
        } else if (d == null) {
            lastDominant = null;
        }
        emo.save();
        maybeNap();
    }

    /** 12s：cruise 时按概率切特殊飞行状态（对齐电脑版行为多样性） */
    private void autoStateTick() {
        handler.postDelayed(this::autoStateTick, 12000);
        if (dead || dnd || workMode || napping || feedPhase != 0) return;
        if (!fly.state.equals("cruise")) return;
        int p = rnd.nextInt(100);
        if (p < 55) return;   // 大部分时间保持巡航
        String[] states = {"dash", "zigzag", "edge_walk", "corner_rest", "drift", "peek",
                "spiral", "window_perch", "text_crawl", "idle_fidget", "hover_jitter",
                "loop", "dive_climb", "figure_eight", "butterfly"};
        int frames = 60 + rnd.nextInt(240);
        if (fly.state.equals("edge_walk")) frames = 0;
        fly.switchTo(states[rnd.nextInt(states.length)], frames);
    }

    /** 30s：挂机检测——90s 无互动发牢骚，20 分钟问是不是睡着了 */
    private void afkTick() {
        handler.postDelayed(this::afkTick, 30000);
        if (dnd || napping || dead) return;
        long idle = System.currentTimeMillis() - lastInteractAt;
        if (idle > 20 * 60000L) {
            if (!afkWarned) {
                afkWarned = true;
                showBubble("喂……你是不是睡着了？都不理我……", 5000);
                emo.add("孤独", 10);
                aiChat("用户已经 20 分钟没有任何操作，你觉得他睡着了，有点失落");
            }
        } else if (idle > 90 * 1000L) {
            afkWarned = false;
            showBubble("嗡嗡！！我在这儿呢！看看我！", 4000);
            fly.switchTo("dash", 40);
        }
    }

    /** 60s：提醒轮询（喝水/久坐/DDL/整点报时）+ 专注到点结算 */
    private void reminderTick() {
        handler.postDelayed(this::reminderTick, 60000);
        // 专注结算优先
        org.json.JSONObject f = Planner.focus();
        if (f.optBoolean("active", false)) {
            if (Planner.focusTick()) {
                String msg = Planner.finishFocus();
                awardAff(10);
                DataStore.setBlood(Math.min(100f, DataStore.getBlood() + 15));
                emo.add("兴奋", 8);
                showBubble(msg + " +10 好感 +15 血池", 5000);
                aiChat("用户完成了一场 " + f.optInt("targetMin", 25) + " 分钟的专注，为他庆祝");
                // 身体闪三下黄光
                flashGlow();
                return;
            }
        }
        if (dnd || napping || dead) return;
        String msg = Planner.pollReminders();
        if (msg != null) {
            showBubble(msg, 6000);
            fly.switchTo("dash", 30);
        }
    }

    /** 专注完成：黄色高光连闪三下 */
    private void flashGlow() {
        final android.graphics.PorterDuffColorFilter yellow =
                new android.graphics.PorterDuffColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_ATOP);
        pet.setColorFilter(yellow);
        for (int i = 1; i <= 3; i++) {
            handler.postDelayed(() -> {
                if (pet.getColorFilter() != null) pet.setColorFilter(null);
                else pet.setColorFilter(yellow);
            }, i * 500L);
        }
        handler.postDelayed(() -> pet.setColorFilter(null), 3500);
    }

    // ---------------- 阶段3：天气 / 主动搭话 / App 感知 ----------------

    /** 天气：wttr.in 免 Key。启动 3s 首查、当天只播一次；之后每 2h 静默刷新缓存 */
    private void weatherTick() {
        String today = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(new Date());
        boolean first = !today.equals(DataStore.sp().getString("weatherDay", ""));
        handler.postDelayed(this::weatherTick, 2 * 3600000L);
        new Thread(() -> {
            String w = fetchWeather();
            if (w == null) {
                if (first) handler.post(() -> showBubble("没查到今天的天气～" + Planner.openCount() + " 个待办还等着你哦", 5000));
                return;
            }
            DataStore.sp().edit().putString("weatherCache", w).putString("weatherDay", today).apply();
            if (first) {
                handler.post(() -> {
                    showBubble("☀️ 今日天气：" + w, 8000);
                    // 顺带催办
                    if (Planner.openCount() > 0) {
                        handler.postDelayed(() -> showBubble("还有 " + Planner.openCount() + " 个待办没完成，今天的水喝了 " + Planner.waterToday() + "/8 杯～", 5000), 8500);
                    }
                });
            }
        }).start();
    }

    /** 拉 wttr.in（格式 ?format=描述|气温|体感|湿度|降雨%） */
    private String fetchWeather() {
        try {
            String city = DataStore.sp().getString("weatherCity", "");
            String u = "https://wttr.in/" + (city.isEmpty() ? "?format=%C|%t|%f|%h|%p&lang=zh" : java.net.URLEncoder.encode(city, "UTF-8") + "?format=%C|%t|%f|%h|%p&lang=zh");
            HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setRequestProperty("User-Agent", "curl/8.0");
            InputStream is = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
            if (is != null) is.close();
            if (c.getResponseCode() != 200) return null;
            String[] p = bos.toString("UTF-8").trim().split("\\|");
            if (p.length < 3) return null;
            String tips = "";
            try {
                int temp = Integer.parseInt(p[1].replaceAll("[^0-9-]", ""));
                if (temp >= 30) tips = "，多喝水！";
                else if (temp <= 5) tips = "，多穿点！";
                int rain = Integer.parseInt(p[4].replaceAll("[^0-9]", ""));
                if (rain >= 50) tips += " 记得带伞☔";
            } catch (Exception ignored) {
            }
            return p[0] + " " + p[1] + "（体感 " + p[2] + "，湿度 " + p[3] + "）" + tips;
        } catch (Exception e) {
            return null;
        }
    }

    /** 主动搭话：90s 轮询，冷却 = AI 说话间隔设置（默认 45s），60% 吐槽屏幕/40% 话题库 */
    private void idleChatTick() {
        handler.postDelayed(this::idleChatTick, 90000);
        if (dnd || napping || dead || workMode || pending) return;
        if (System.currentTimeMillis() - Math.max(lastAiAt, lastInteractAt) < 45000) return;
        String app = currentAppLabel();
        boolean talkApp = DataStore.getBool("appSense", true) && rnd.nextInt(100) < 60 && app != null;
        String topic = talkApp ? "（用户正在用 " + app + "，随口吐槽或调侃一句，要短）"
                : randomTopic();
        if (topic == null) return;
        aiChat("主动搭话：" + topic);
    }

    private String lastTopic = null;

    private String randomTopic() {
        java.util.List<String> ts = Quotes.get("topics");
        if (ts.isEmpty()) return null;
        String t = ts.get(rnd.nextInt(ts.size()));
        if (t.equals(lastTopic) && ts.size() > 1) t = ts.get(rnd.nextInt(ts.size()));
        lastTopic = t;
        return t;
    }
    
    /** 前台应用感知：25s 轮询。会议 App→自动勿扰；娱乐/办公类→概率吐槽 */
    private void appSenseTick() {
        handler.postDelayed(this::appSenseTick, 25000);
        if (dead || napping) return;
        if (!DataStore.getBool("appSense", true)) return;   // 感知总开关
        String app = currentAppLabel();
        if (app == null) return;
        String appKey = appKeyOf(app);
        // 会议自动勿扰
        boolean meeting = appKeyOf(app).equals("会议");
        if (meeting && !dnd && !DataStore.getBool("autoDndOn", false)) {
            DataStore.putBool("autoDndOn", true);
            toggleDnd();   // 开勿扰（内部会播"开会呢"）
            return;
        }
        if (!meeting && DataStore.getBool("autoDndOn", false)) {
            DataStore.putBool("autoDndOn", false);
            if (dnd) toggleDnd();   // 散会恢复
            return;
        }
        if (dnd) return;
        // 冷却 20 分钟 + 概率 40%
        long now = System.currentTimeMillis();
        String k = "appsense_" + appKey;
        if (now - DataStore.getLong(k, 0) < 1200000L) return;
        if (rnd.nextInt(100) >= 40) return;
        DataStore.putLong(k, now);
        String line = appSenseLine(appKey);
        if (line == null) return;
        showBubble(line, 4500);
        if (appKey.equals("音乐")) fly.switchTo("butterfly", 200);
    }

    /** 拿前台应用标签（无障碍拿不到，用 UsageStatsManager 最近事件近似；无权限返回 null） */
    private String currentAppLabel() {
        try {
            android.app.usage.UsageStatsManager usm = (android.app.usage.UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            java.util.List<android.app.usage.UsageStats> l = usm.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, now - 60000L, now);
            if (l == null || l.isEmpty()) return null;
            android.app.usage.UsageStats top = null;
            for (android.app.usage.UsageStats s : l) {
                if (top == null || s.getLastTimeUsed() > top.getLastTimeUsed()) top = s;
            }
            if (top == null || top.getLastTimeUsed() < now - 120000L) return null;
            String pkg = top.getPackageName();
            String nm = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString();
            return nm;
        } catch (Exception e) {
            return null;
        }
    }

    /** 给设置页显示当前前台应用名（App 感知开且查得到才有值） */
    public String currentAppLabelPublic() {
        if (!DataStore.getBool("appSense", true)) return null;
        return currentAppLabel();
    }

    /** 应用归类（对应电脑版 APP_KEYWORDS 六类+音乐+会议） */
    private String appKeyOf(String name) {
        String n = name == null ? "" : name;
        if (containsAny(n, "会议", "腾讯会议", "Zoom", "Teams", "Webex", "钉钉", "飞书")) return "会议";
        if (containsAny(n, "Word", "Excel", "PPT", "WPS", "办公", "文档", "笔记", "Notion", "备忘")) return "办公";
        if (containsAny(n, "哔哩", "B站", "bilibili", "抖音", "快手", "YouTube", "优酷", "爱奇艺", "腾讯视频", "芒果")) return "视频";
        if (containsAny(n, "微信", "QQ", "钉钉", "飞书", "Telegram", "WhatsApp", "微博", "小红书")) return "聊天";
        if (containsAny(n, "网易云", "QQ音乐", "音乐", "Spotify", "酷狗", "酷我", "汽水")) return "音乐";
        if (containsAny(n, "游戏", "原神", "王者", "和平精英", "Steam", "蛋仔", "元梦", "金铲铲", "崩坏")) return "游戏";
        if (containsAny(n, "浏览器", "Chrome", "Edge", "夸克", "UC", "百度", "知乎", "淘宝", "京东", "拼多多", "支付宝")) return "购物浏览";
        return "其他";
    }

    private boolean containsAny(String s, String... kws) {
        for (String k : kws) if (s.contains(k)) return true;
        return false;
    }

    private String appSenseLine(String key) {
        java.util.List<String> pool = new java.util.ArrayList<String>();
        for (String line : Quotes.get("appsense")) {
            String[] p = line.split("\\|");
            if (p.length == 2 && p[0].equals(key)) pool.add(p[1]);
        }
        if (pool.isEmpty()) return null;
        return pool.get(rnd.nextInt(pool.size()));
    }
    
    // ---------------- 阶段4：随机事件 / 小剧场 / 成就 / 教程 ----------------

    /** 3 分钟轮询：随机事件 + 小游戏 + 小剧场（均带冷却与概率门） */
    private void extrasTick() {
        handler.postDelayed(this::extrasTick, 180000);
        if (dnd || napping || dead || workMode || pending) return;
        // 随机事件
        String ev = Extras.randomEvent(rnd);
        if (ev != null) {
            showBubble(ev, 6000);
            return;
        }
        // 小游戏
        String[] game = Extras.miniGame(rnd);
        if (game != null) {
            showBubble("🎮 " + game[0] + "\n" + game[1], 9000);
            return;
        }
        // 小剧场
        String[] th = Extras.theater(rnd);
        if (th != null) {
            theaterDialog(th);
        }
        // 好感里程碑纪念小剧场（跨档 8 秒后）
        String pending = DataStore.getPendingTheater();
        if (!pending.isEmpty()) {
            theaterDialog(new String[]{"好感度跨越 " + pending.replace("aff", "") + " 的纪念时刻！\n它想跟你玩个大的", "认真庆祝一下！", "低调路过…"});
        }
    }

    /** 小剧场二选一弹窗（从 Service 弹需 FLAG_ACTIVITY_NEW_TASK） */
    private void theaterDialog(String[] th) {
        Intent i = new Intent(this, TheaterActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("scene", th[0]);
        i.putExtra("a", th[1]);
        i.putExtra("b", th[2]);
        startActivity(i);
    }

    /** 2 分钟：成就检查 */
    private void achieveTick() {
        handler.postDelayed(this::achieveTick, 120000);
        String ach = Extras.checkAchievements();
        if (ach != null) {
            showBubble(ach, 6000);
            emo.add("兴奋", 10);
        }
    }

    /** 首次教程：7 步气泡序列 */
    private void playTutorial() {
        java.util.List<String> steps = Quotes.get("tutorial");
        for (int i = 0; i < steps.size(); i++) {
            final String t = steps.get(i);
            handler.postDelayed(() -> showBubble(t, 4200), 800L + i * 5000L);
        }
        handler.postDelayed(() -> {
            DataStore.putBool("tutorialDone", true);
            showBubble("教程完啦！多多关照～嗡嗡嗡", 3000);
        }, 800L + steps.size() * 5000L);
    }
    
    // ---------------- Flyer.Host 实现 ----------------

    @Override
    public float px() { return px; }

    @Override
    public float py() { return py; }

    @Override
    public void setPos(float x, float y) { px = x; py = y; }

    @Override
    public int screenW() { return screenW; }

    @Override
    public int screenH() { return screenH; }

    @Override
    public int size() { return curSize(); }

    @Override
    public float speedMul() { return speedMul; }

    @Override
    public float glideFriction() { return frictionFor(glideLevel()); }

    @Override
    public Random rnd() { return rnd; }

    @Override
    public Handler handler() { return handler; }

    // ---------------- 阶段7：时间感知 / 拉手聊天 / 惯性档位 ----------------

    /** 惯性摩擦系数：档位 0关(1.0=无惯性) 1轻(0.90) 2中(0.94) 3强(0.965)——数值越大滑得越远 */
    public int glideLevel() { return DataStore.getInt("glideLevel", 2); }

    public void setGlideLevel(int lv) {
        DataStore.putInt("glideLevel", Math.max(0, Math.min(3, lv)));
    }

    private static float frictionFor(int lv) {
        switch (lv) {
            case 0: return 0f;      // 关：松手不滑行
            case 1: return 0.90f;
            case 3: return 0.965f;
            default: return 0.94f;
        }
    }

    /** 时间感知：返回 [时段名, 问候语]；凌晨/清晨/上午/中午/下午/傍晚/晚上/深夜 */
    public static String[] timePeriod() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                String[] names = {"凌晨", "清晨", "上午", "中午", "下午", "傍晚", "晚上", "深夜"};
        String[] keys = {"period_dawn", "period_morning", "period_am", "period_noon",
                "period_pm", "period_dusk", "period_night", "period_late"};
        int idx;
        if (h < 5) idx = 0;
        else if (h < 8) idx = 1;
        else if (h < 12) idx = 2;
        else if (h < 14) idx = 3;
        else if (h < 18) idx = 4;
        else if (h < 20) idx = 5;
        else if (h < 23) idx = 6;
        else idx = 7;
        return new String[]{names[idx], Quotes.get(keys[idx]).get(0)};
    }

    /** 60s 轮询：跨时段播报问候（挂在 moodTick 里调）；AI 提示词也带上当前时间 */
    private void timeSenseTick() {
        String[] p = timePeriod();
        if (!p[0].equals(lastPeriod)) {
            boolean first = lastPeriod == null;
            lastPeriod = p[0];
            if (!first && !dnd && !napping && !dead) {
                showBubble("🕐 " + p[0] + "了。" + p[1], 4000);
            }
        }
    }

    /** 屏幕右缘小拉手：点开=悬浮聊天框；聊天框可输可发，还显示当前前台 App 名 */
    private void createTabHandle() {
        tabHandle = new TextView(this);
        tabHandle.setText("💬");
        tabHandle.setTextSize(18);
        tabHandle.setTextColor(Color.WHITE);
        tabHandle.setGravity(Gravity.CENTER);
        tabHandle.setPadding(0, dp(10), 0, dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#CC1B2A4A"));
        g.setCornerRadii(new float[]{dp(14), 0, 0, dp(14), dp(14), 0, 0, dp(14)});   // 左圆右直，贴边
        tabHandle.setBackground(g);
        tabLP = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        tabLP.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        tabHandle.setOnClickListener(v -> toggleChatPanel());
        try { wm.addView(tabHandle, tabLP); } catch (Exception ignored) {}
        // 初始隐藏与否跟设置走
        tabHandle.setVisibility(DataStore.getBool("tabHandle", true) ? View.VISIBLE : View.GONE);
    }

    public void setTabHandleVisible(boolean on) {
        DataStore.putBool("tabHandle", on);
        if (tabHandle != null) tabHandle.setVisibility(on ? View.VISIBLE : View.GONE);
        if (!on && chatPanel != null) chatPanel.setVisibility(View.GONE);
    }

    /** 悬浮聊天框：挂屏幕右侧（宽 78%），顶部显示当前前台应用名 */
    private void toggleChatPanel() {
        if (chatPanel == null) {
            buildChatPanel();
        }
        if (chatPanel.getVisibility() == View.VISIBLE) {
            chatPanel.setVisibility(View.GONE);
            // 收起时保持不可聚焦（不挡底层操作）
            chatLP.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            try { wm.updateViewLayout(chatPanel, chatLP); } catch (Exception ignored) {}
        } else {
            refreshChatStatus();
            // 展开时允许输入框拿焦点弹软键盘
            chatLP.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            try { wm.updateViewLayout(chatPanel, chatLP); } catch (Exception ignored) {}
            chatPanel.setVisibility(View.VISIBLE);
            chatIn.requestFocus();
            refreshChatLog();
        }
    }

    private void buildChatPanel() {
        chatPanel = new LinearLayout(this);
        chatPanel.setOrientation(LinearLayout.VERTICAL);
        chatPanel.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E8FFFFFF"));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(2), Color.parseColor("#111111"));
        chatPanel.setBackground(bg);

        chatStatus = new TextView(this);
        chatStatus.setTextSize(11);
        chatStatus.setTextColor(Color.parseColor("#666666"));
        chatStatus.setPadding(dp(4), 0, dp(4), dp(4));
        chatPanel.addView(chatStatus);

        TextView log = new TextView(this);
        log.setTextSize(12);
        log.setTextColor(Color.parseColor("#111111"));
        log.setMaxLines(8);
        chatLogView = log;
        ScrollView sc = new ScrollView(this);
        sc.addView(log);
        chatPanel.addView(sc, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        chatIn = new EditText(this);
        chatIn.setHint("跟它说点什么…");
        chatIn.setTextSize(13);
        chatIn.setMaxLines(1);
        chatIn.setTextColor(Color.parseColor("#111111"));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(chatIn, ip);
        TextView send = new TextView(this);
        send.setText("发送");
        send.setTextSize(13);
        send.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        send.setTextColor(Color.parseColor("#111111"));
        send.setGravity(Gravity.CENTER);
        send.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable sb = new GradientDrawable();
        sb.setColor(Color.WHITE);
        sb.setCornerRadius(dp(10));
        sb.setStroke(dp(2), Color.parseColor("#111111"));
        send.setBackground(sb);
        send.setOnClickListener(v -> {
            String s = chatIn.getText().toString().trim();
            if (s.isEmpty()) return;
            chatIn.setText("");
            userChat(s);
            refreshChatLog();
        });
        row.addView(send);
        chatPanel.addView(row);

        chatLP = new WindowManager.LayoutParams(
                (int) (screenW * 0.78f), (int) (screenH * 0.55f),
                overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,   // 让输入框能拿焦点弹键盘
                PixelFormat.TRANSLUCENT);
        chatLP.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        try { wm.addView(chatPanel, chatLP); } catch (Exception ignored) {}
        chatPanel.setVisibility(View.GONE);
    }

    private TextView chatLogView;

    private void refreshChatLog() {
        if (chatLogView == null) return;
        chatLogView.setText(chatLogText().isEmpty() ? "（还没聊过天）" : chatLogText());
    }

    /** 聊天框顶部状态行：时间时段 + 前台 App 名 */
    private void refreshChatStatus() {
        if (chatStatus == null) return;
        String[] p = timePeriod();
        String app = currentAppLabel();
        String appTxt = (app == null)
                ? "（App 感知未生效：去设置页开『App感知』并授『使用情况访问权限』）"
                : "当前应用：" + app;
        chatStatus.setText("🕐 " + p[0] + "　·　" + appTxt);
    }

    /** 感知开关（设置页用） */
    public void setAppSense(boolean on) {
        DataStore.putBool("appSense", on);
    }

    // ---------------- 阶段5：整蛊模式开关（设置页调用） ----------------

    /** 蚊群帧图来源 */
    public int mosquitoRes() { return cruiseF[0]; }

    public void startPrank(int count) {
        if (prank == null) prank = new PrankEngine(new PrankEngine.Host() {
            @Override public WindowManager wm() { return wm; }
            @Override public android.os.Handler handler() { return handler; }
            @Override public Random rnd() { return rnd; }
            @Override public int screenW() { return screenW; }
            @Override public int screenH() { return screenH; }
            @Override public PetService pet() { return PetService.this; }
        });
        prankMode = true;
        // 陪伴蚊先隐藏，让位给蚊群
        pet.setVisibility(View.GONE);
        hideBubble();
        prank.start(count);
        showBubble("蚊群入侵！快速点击拍打它们！拍满 40 只或撑过 10 分钟出 BOSS！", 6000);
    }

    public void stopPrank() {
        if (prank != null) prank.stop();
        prankMode = false;
        pet.setVisibility(View.VISIBLE);
    }

    public String prankScoreText() {
        org.json.JSONObject o = DataStore.obj("prankScore");
        return "最高第 " + o.optInt("bestWave", 0) + " 波 · 累计击杀 " + o.optInt("totalKills", 0)
                + (prankMode && prank != null ? " · 本局已杀 " + prank.kills() : "");
    }

    // ---------------- App 内设置窗口相关 ----------------

    public void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void openPlanner() {
        Intent i = new Intent(this, PlannerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void userChat(String text) {
        lastInteractAt = System.currentTimeMillis();
        DataStore.putInt("talkCount", DataStore.getInt("talkCount", 0) + 1);
        if (DataStore.getInt("talkCount", 0) >= 50 && !DataStore.getBool("ach_talk", false)) {
            DataStore.putBool("ach_talk", true);
            showBubble("🏆 解锁成就：话痨之友 💬", 5000);
        }
        logChat("你", text);
        aiChat("用户对你说：" + text);
    }

    public void setSpeedMul(float v) {
        speedMul = Math.max(0.3f, Math.min(4f, v));
        sp.edit().putFloat("speedMul", speedMul).apply();
    }

    public float getSpeedMul() { return speedMul; }

    public void setAffection(int v) {
        affection = Math.max(0, v);
        DataStore.setAffRaw(affection);
    }

    public void applyScaleNow() {
        applyPetSize();
    }

    private void initApi() {
        apiBase = sp.getString("apiBase", URL_API);
        apiKey = sp.getString("apiKey", KEY_API);
        apiModel = sp.getString("apiModel", MODEL);
    }

    /** 接口地址智能补全（静态版，供 Memory 副 API 用） */
    static String normalizeEndpointStatic(String base) {
        String u = base == null ? "" : base.trim();
        if (u.length() == 0) return u;
        if (u.endsWith("#")) return u.substring(0, u.length() - 1).trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        if (u.endsWith("/chat/completions")) return u;
        return u + "/chat/completions";
    }

    /** 接口地址智能补全：
     *  填根地址 https://api.x.com            -> https://api.x.com/chat/completions
     *  填版本根 https://api.x.com/v1 或 /v3   -> .../v1/chat/completions
     *  已填完整 .../chat/completions          -> 原样使用
     *  末尾加 #                               -> 强制原样（特殊网关） */
    private static String normalizeEndpoint(String base) {
        String u = base == null ? "" : base.trim();
        if (u.length() == 0) return u;
        if (u.endsWith("#")) return u.substring(0, u.length() - 1).trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        if (u.endsWith("/chat/completions")) return u;
        return u + "/chat/completions";
    }

    public void setApi(String base, String key, String model) {
        if (base != null && base.trim().length() > 0) apiBase = base.trim();
        if (key != null && key.trim().length() > 0) apiKey = key.trim();
        if (model != null && model.trim().length() > 0) apiModel = model.trim();
        sp.edit().putString("apiBase", apiBase).putString("apiKey", apiKey).putString("apiModel", apiModel).apply();
    }

    /** 从聊天端点推出模型列表端点：.../v1/chat/completions -> .../v1/models；根地址 -> .../models */
    static String modelsEndpoint(String base) {
        String u = base == null ? "" : base.trim();
        if (u.length() == 0) return u;
        if (u.endsWith("#")) u = u.substring(0, u.length() - 1).trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        if (u.endsWith("/chat/completions")) {
            u = u.substring(0, u.length() - "/chat/completions".length());
            while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
            return u + "/models";
        }
        if (u.endsWith("/models")) return u;
        return u + "/models";
    }

    /** 拉取服务商模型列表（GET /models，标准 OpenAI 格式 {"data":[{"id":...}]}） */
    public void fetchModels(final ModelsCallback cb) {
        final String url = modelsEndpoint(apiBase);
        new Thread(() -> {
            String err = null;
            java.util.List<String> names = new java.util.ArrayList<String>();
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Authorization", "Bearer " + apiKey);
                c.setConnectTimeout(15000);
                c.setReadTimeout(20000);
                InputStream is = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while (is != null && (n = is.read(buf)) > 0) bos.write(buf, 0, n);
                if (is != null) is.close();
                if (c.getResponseCode() != 200) {
                    err = "HTTP " + c.getResponseCode();
                } else {
                    JSONObject j = new JSONObject(bos.toString("UTF-8"));
                    org.json.JSONArray arr = j.optJSONArray("data");
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            String id = arr.getJSONObject(i).optString("id", "");
                            if (!id.isEmpty()) names.add(id);
                        }
                    }
                    if (names.isEmpty()) err = "返回里没有模型（格式不对？）";
                }
            } catch (Exception e) {
                err = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
            }
            final java.util.List<String> fNames = names;
            final String fErr = err;
            handler.post(() -> cb.onResult(fNames, fErr));
        }).start();
    }

    interface ModelsCallback {
        void onResult(java.util.List<String> models, String error);
    }

    private static final java.util.Map<String, String[]> DEFAULT_QUOTES = new java.util.HashMap<String, String[]>();

    private void loadQuotes() {
        DEFAULT_QUOTES.clear();
        DEFAULT_QUOTES.put("tap", new String[]{"嗯？", "别闹…", "干嘛？", "戳啥呢"});
        DEFAULT_QUOTES.put("throw", new String[]{"哎呀！", "你干什么！", "喂——"});
        DEFAULT_QUOTES.put("revive", new String[]{"哼，我会复活的…你等着"});
        DEFAULT_QUOTES.put("sleep", new String[]{"Zzz…", "好困…", "抱着挺舒服…"});
        DEFAULT_QUOTES.put("fallback", new String[]{"嗡～信号不太好，等会儿再聊", "（信号弱）先自己玩会儿…", "嗡嗡…听不清，再说一遍？"});
        DEFAULT_QUOTES.put("work_on", new String[]{"好嘞，进入工作状态！"});
        DEFAULT_QUOTES.put("work_off", new String[]{"下班啦！"});
        for (String k : DEFAULT_QUOTES.keySet()) {
            java.util.List<String> l = new java.util.ArrayList<String>();
            for (String s : DEFAULT_QUOTES.get(k)) l.add(s);
            quotes.put(k, l);
        }
        String saved = sp.getString("quotes", null);
        if (saved == null) return;
        try {
            JSONObject o = new JSONObject(saved);
            java.util.Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                org.json.JSONArray arr = o.getJSONArray(k);
                java.util.List<String> l = new java.util.ArrayList<String>();
                for (int i = 0; i < arr.length(); i++) {
                    String s = arr.getString(i).trim();
                    if (s.length() > 0) l.add(s);
                }
                if (!l.isEmpty()) quotes.put(k, l);
            }
        } catch (Exception ignored) {}
    }

    public void saveQuoteGroup(String key, java.util.List<String> lines) {
        if (lines == null || lines.isEmpty()) return;
        quotes.put(key, lines);
        JSONObject o = new JSONObject();
        for (java.util.Map.Entry<String, java.util.List<String>> e : quotes.entrySet()) {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (String s : e.getValue()) arr.put(s);
            try { o.put(e.getKey(), arr); } catch (Exception ignored) {}
        }
        sp.edit().putString("quotes", o.toString()).apply();
    }

    public String q(String key) {
        return Quotes.pick(key, rnd);
    }

    public void toggleWork() {
        workMode = !workMode;
        if (workMode) {
            state = "work";
            showBubble(q("work_on"), 2000);
        } else {
            state = "cruise";
            randomizeVelocity();
            showBubble(q("work_off"), 2000);
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(dm);
            screenW = dm.widthPixels;
            screenH = dm.heightPixels;
            clampPet();
            petLP.x = (int) px;
            petLP.y = (int) py;
            wm.updateViewLayout(pet, petLP);
        } catch (Exception ignored) {}
    }

    public void toggleDnd() {
        dnd = !dnd;
        if (dnd) {
            fly.switchTo("edge_walk", 0);
            showBubble("开会呢/看书呢，我先不打扰了 🤫", 2000);
        } else {
            fly.switchTo("cruise", 0);
            showBubble("我回来啦！继续飞～", 2000);
        }
    }

    public int daysCount() {
        return (int) ((System.currentTimeMillis() - firstAt) / 86400000L) + 1;
    }

    public String relationTitle() {
        return DataStore.titleFor(DataStore.getAff());
    }

    public String feedStatusText() {
        return "饱食度 " + (int) DataStore.getSatiety() + "/100 · 血池 " + (int) DataStore.getBlood()
                + "/100 · 累计献血 " + (int) DataStore.getBloodTotal() + "（" + DataStore.bloodTitle() + "）";
    }

    public void logChat(String who, String text) {
        synchronized (chatLog) {
            chatLog.add(who + "：" + text);
            while (chatLog.size() > 40) chatLog.remove(0);
        }
    }

    public String chatLogText() {
        synchronized (chatLog) {
            StringBuilder sb = new StringBuilder();
            for (String l : chatLog) sb.append(l).append('\n');
            return sb.toString();
        }
    }

    // ---------------- 好感度 / 台词 ----------------

    private void addAffection(int n) {
        // 旧路径仅内部兼容；新代码一律走 awardAff（含每日上限与里程碑）
        awardAff(n);
    }

    private String pick(String... a) { return a[rnd.nextInt(a.length)]; }

    private static final String[] FALLBACK = {
            "嗡～信号不太好，等会儿再聊", "（信号弱）先自己玩会儿…", "嗡嗡…听不清，再说一遍？"};

    // ---------------- AI 对话（原生转发，无 CORS 问题） ----------------

    public void aiChat(String situation) {
        if (pending) return;
        pending = true;
        lastAiAt = System.currentTimeMillis();
        showBubble("对方正在回应中...", 60000);
        final String body;
        try {
            JSONObject o = new JSONObject();
            o.put("model", apiModel);
            o.put("temperature", 0.85);
            org.json.JSONArray msgs = new org.json.JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", persona()))
                    .put(new JSONObject().put("role", "user")
                            .put("content", "【当前情景】" + situation + "\n【当前设备】用户手机\n【好感度】" + DataStore.getAff()
                                    + "（" + DataStore.titleFor(DataStore.getAff()) + "）\n【情绪】" + emo.describe()
                                    + "\n【饲养】" + feedStatusText() + "\n【饲养天数】第 " + daysCount() + " 天"
                                    + "\n【当前时间】" + new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(new Date())
                                    + "（" + timePeriod()[0] + "）"
                                    + (DataStore.sp().getString("weatherCache", "").isEmpty() ? "" : "\n【今日天气】" + DataStore.sp().getString("weatherCache", ""))
                                    + "\n【前台应用】" + (currentAppLabel() == null ? "未知（无权限）" : currentAppLabel())
                                    + Memory.promptBlock()
                                    + (Memory.userPersona().isEmpty() ? "" : "\n【用户身份（用户自述）】" + Memory.userPersona())
                                    + "\n\n请用你的口吻回应，两句话以内，不要客套。"));
            // 短期记忆：最近 10 轮对话注入（保持因果顺序）
            java.util.List<String> hist;
            synchronized (chatLog) {
                hist = new java.util.ArrayList<String>(chatLog);
            }
            int start = Math.max(0, hist.size() - 20);
            for (int i = start; i < hist.size(); i++) {
                String line = hist.get(i);
                String role = line.startsWith("你：") ? "user" : "assistant";
                String content = line.startsWith("你：") ? line.substring(2) : line.substring(line.indexOf("：") + 1);
                if (content.trim().isEmpty()) continue;
                msgs.put(new JSONObject().put("role", role).put("content", content));
            }
            o.put("messages", msgs);
            body = o.toString();
        } catch (Exception e) {
            pending = false;
            return;
        }
        new Thread(() -> {
            String reply = null;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(normalizeEndpoint(apiBase)).openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("Authorization", "Bearer " + apiKey);
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
            if (fReply != null) {
                // 记忆本：原始记忆追加（情景+回复），攒到阈值由副 API 自动整理
                Memory.append(situation, fReply);
                if (Memory.shouldDigest()) {
                    Memory.digest(PetService.this, (ok, d, e) -> {
                        if (ok) showBubble("🧠 记忆整理完毕（" + Memory.keep() + " 条长期记忆入库）", 4000);
                    });
                }
            }
            logChat("蚊", fReply == null ? q("fallback") : fReply);
            handler.post(() -> {
                pending = false;
                if (fReply == null) showBubble(q("fallback"), 3000);
                else showBubble(fReply, 6000);
            });
        }).start();
    }

    // ---------------- 生命周期 ----------------

    @Override
    public int onStartCommand(Intent i, int f, int id) { return START_STICKY; }

    @Override
    public void onDestroy() {
        instance = null;
        handler.removeCallbacksAndMessages(null);
        emo.save();
        for (View v : new View[]{pet, bubble, tabHandle, chatPanel}) {
            if (v != null) try { wm.removeView(v); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}
