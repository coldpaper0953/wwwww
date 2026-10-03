package com.weng.weng;

/** 版本差异（classic = 原版嗡嗡嗡）：保留内置加密人设；宠物形象不可自定义。 */
public class Edition {

    /** 是否允许用户自定义宠物形象（设置页入口是否出现） */
    public static final boolean CUSTOM_SKIN = false;

    /** 自更新 Release 的 tag 前缀（原版 tag 形如 v2.11.0） */
    public static final String RELEASE_TAG_PREFIX = "v";

    /** 自更新来源仓库 */
    public static final String GITHUB_REPO = "coldpaper0953/wwwww";

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

    /** 解密内嵌人设（与电脑版同一套 W1 方案：SHA-256(key||counter) CTR + base64） */
    public static String persona() {
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
}
