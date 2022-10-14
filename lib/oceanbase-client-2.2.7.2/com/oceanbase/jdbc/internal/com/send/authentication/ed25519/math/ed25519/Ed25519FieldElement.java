// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519;

import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.Utils;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Field;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.FieldElement;

public class Ed25519FieldElement extends FieldElement
{
    private static final byte[] ZERO;
    final int[] t;
    
    public Ed25519FieldElement(final Field f, final int[] t) {
        super(f);
        if (t.length != 10) {
            throw new IllegalArgumentException("Invalid radix-2^51 representation");
        }
        this.t = t;
    }
    
    @Override
    public boolean isNonZero() {
        final byte[] s = this.toByteArray();
        return Utils.equal(s, Ed25519FieldElement.ZERO) == 0;
    }
    
    @Override
    public FieldElement add(final FieldElement val) {
        final int[] g = ((Ed25519FieldElement)val).t;
        final int[] h = new int[10];
        for (int i = 0; i < 10; ++i) {
            h[i] = this.t[i] + g[i];
        }
        return new Ed25519FieldElement(this.f, h);
    }
    
    @Override
    public FieldElement subtract(final FieldElement val) {
        final int[] g = ((Ed25519FieldElement)val).t;
        final int[] h = new int[10];
        for (int i = 0; i < 10; ++i) {
            h[i] = this.t[i] - g[i];
        }
        return new Ed25519FieldElement(this.f, h);
    }
    
    @Override
    public FieldElement negate() {
        final int[] h = new int[10];
        for (int i = 0; i < 10; ++i) {
            h[i] = -this.t[i];
        }
        return new Ed25519FieldElement(this.f, h);
    }
    
    @Override
    public FieldElement multiply(final FieldElement val) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: checkcast       Lcom/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement;
        //     4: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //     7: astore_2        /* g */
        //     8: bipush          19
        //    10: aload_2         /* g */
        //    11: iconst_1       
        //    12: iaload         
        //    13: imul           
        //    14: istore_3        /* g1_19 */
        //    15: bipush          19
        //    17: aload_2         /* g */
        //    18: iconst_2       
        //    19: iaload         
        //    20: imul           
        //    21: istore          g2_19
        //    23: bipush          19
        //    25: aload_2         /* g */
        //    26: iconst_3       
        //    27: iaload         
        //    28: imul           
        //    29: istore          g3_19
        //    31: bipush          19
        //    33: aload_2         /* g */
        //    34: iconst_4       
        //    35: iaload         
        //    36: imul           
        //    37: istore          g4_19
        //    39: bipush          19
        //    41: aload_2         /* g */
        //    42: iconst_5       
        //    43: iaload         
        //    44: imul           
        //    45: istore          g5_19
        //    47: bipush          19
        //    49: aload_2         /* g */
        //    50: bipush          6
        //    52: iaload         
        //    53: imul           
        //    54: istore          g6_19
        //    56: bipush          19
        //    58: aload_2         /* g */
        //    59: bipush          7
        //    61: iaload         
        //    62: imul           
        //    63: istore          g7_19
        //    65: bipush          19
        //    67: aload_2         /* g */
        //    68: bipush          8
        //    70: iaload         
        //    71: imul           
        //    72: istore          g8_19
        //    74: bipush          19
        //    76: aload_2         /* g */
        //    77: bipush          9
        //    79: iaload         
        //    80: imul           
        //    81: istore          g9_19
        //    83: iconst_2       
        //    84: aload_0         /* this */
        //    85: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //    88: iconst_1       
        //    89: iaload         
        //    90: imul           
        //    91: istore          f1_2
        //    93: iconst_2       
        //    94: aload_0         /* this */
        //    95: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //    98: iconst_3       
        //    99: iaload         
        //   100: imul           
        //   101: istore          f3_2
        //   103: iconst_2       
        //   104: aload_0         /* this */
        //   105: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   108: iconst_5       
        //   109: iaload         
        //   110: imul           
        //   111: istore          f5_2
        //   113: iconst_2       
        //   114: aload_0         /* this */
        //   115: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   118: bipush          7
        //   120: iaload         
        //   121: imul           
        //   122: istore          f7_2
        //   124: iconst_2       
        //   125: aload_0         /* this */
        //   126: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   129: bipush          9
        //   131: iaload         
        //   132: imul           
        //   133: istore          f9_2
        //   135: aload_0         /* this */
        //   136: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   139: iconst_0       
        //   140: iaload         
        //   141: i2l            
        //   142: aload_2         /* g */
        //   143: iconst_0       
        //   144: iaload         
        //   145: i2l            
        //   146: lmul           
        //   147: lstore          f0g0
        //   149: aload_0         /* this */
        //   150: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   153: iconst_0       
        //   154: iaload         
        //   155: i2l            
        //   156: aload_2         /* g */
        //   157: iconst_1       
        //   158: iaload         
        //   159: i2l            
        //   160: lmul           
        //   161: lstore          f0g1
        //   163: aload_0         /* this */
        //   164: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   167: iconst_0       
        //   168: iaload         
        //   169: i2l            
        //   170: aload_2         /* g */
        //   171: iconst_2       
        //   172: iaload         
        //   173: i2l            
        //   174: lmul           
        //   175: lstore          f0g2
        //   177: aload_0         /* this */
        //   178: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   181: iconst_0       
        //   182: iaload         
        //   183: i2l            
        //   184: aload_2         /* g */
        //   185: iconst_3       
        //   186: iaload         
        //   187: i2l            
        //   188: lmul           
        //   189: lstore          f0g3
        //   191: aload_0         /* this */
        //   192: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   195: iconst_0       
        //   196: iaload         
        //   197: i2l            
        //   198: aload_2         /* g */
        //   199: iconst_4       
        //   200: iaload         
        //   201: i2l            
        //   202: lmul           
        //   203: lstore          f0g4
        //   205: aload_0         /* this */
        //   206: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   209: iconst_0       
        //   210: iaload         
        //   211: i2l            
        //   212: aload_2         /* g */
        //   213: iconst_5       
        //   214: iaload         
        //   215: i2l            
        //   216: lmul           
        //   217: lstore          f0g5
        //   219: aload_0         /* this */
        //   220: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   223: iconst_0       
        //   224: iaload         
        //   225: i2l            
        //   226: aload_2         /* g */
        //   227: bipush          6
        //   229: iaload         
        //   230: i2l            
        //   231: lmul           
        //   232: lstore          f0g6
        //   234: aload_0         /* this */
        //   235: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   238: iconst_0       
        //   239: iaload         
        //   240: i2l            
        //   241: aload_2         /* g */
        //   242: bipush          7
        //   244: iaload         
        //   245: i2l            
        //   246: lmul           
        //   247: lstore          f0g7
        //   249: aload_0         /* this */
        //   250: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   253: iconst_0       
        //   254: iaload         
        //   255: i2l            
        //   256: aload_2         /* g */
        //   257: bipush          8
        //   259: iaload         
        //   260: i2l            
        //   261: lmul           
        //   262: lstore          f0g8
        //   264: aload_0         /* this */
        //   265: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   268: iconst_0       
        //   269: iaload         
        //   270: i2l            
        //   271: aload_2         /* g */
        //   272: bipush          9
        //   274: iaload         
        //   275: i2l            
        //   276: lmul           
        //   277: lstore          f0g9
        //   279: aload_0         /* this */
        //   280: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   283: iconst_1       
        //   284: iaload         
        //   285: i2l            
        //   286: aload_2         /* g */
        //   287: iconst_0       
        //   288: iaload         
        //   289: i2l            
        //   290: lmul           
        //   291: lstore          f1g0
        //   293: iload           f1_2
        //   295: i2l            
        //   296: aload_2         /* g */
        //   297: iconst_1       
        //   298: iaload         
        //   299: i2l            
        //   300: lmul           
        //   301: lstore          f1g1_2
        //   303: aload_0         /* this */
        //   304: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   307: iconst_1       
        //   308: iaload         
        //   309: i2l            
        //   310: aload_2         /* g */
        //   311: iconst_2       
        //   312: iaload         
        //   313: i2l            
        //   314: lmul           
        //   315: lstore          f1g2
        //   317: iload           f1_2
        //   319: i2l            
        //   320: aload_2         /* g */
        //   321: iconst_3       
        //   322: iaload         
        //   323: i2l            
        //   324: lmul           
        //   325: lstore          f1g3_2
        //   327: aload_0         /* this */
        //   328: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   331: iconst_1       
        //   332: iaload         
        //   333: i2l            
        //   334: aload_2         /* g */
        //   335: iconst_4       
        //   336: iaload         
        //   337: i2l            
        //   338: lmul           
        //   339: lstore          f1g4
        //   341: iload           f1_2
        //   343: i2l            
        //   344: aload_2         /* g */
        //   345: iconst_5       
        //   346: iaload         
        //   347: i2l            
        //   348: lmul           
        //   349: lstore          f1g5_2
        //   351: aload_0         /* this */
        //   352: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   355: iconst_1       
        //   356: iaload         
        //   357: i2l            
        //   358: aload_2         /* g */
        //   359: bipush          6
        //   361: iaload         
        //   362: i2l            
        //   363: lmul           
        //   364: lstore          f1g6
        //   366: iload           f1_2
        //   368: i2l            
        //   369: aload_2         /* g */
        //   370: bipush          7
        //   372: iaload         
        //   373: i2l            
        //   374: lmul           
        //   375: lstore          f1g7_2
        //   377: aload_0         /* this */
        //   378: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   381: iconst_1       
        //   382: iaload         
        //   383: i2l            
        //   384: aload_2         /* g */
        //   385: bipush          8
        //   387: iaload         
        //   388: i2l            
        //   389: lmul           
        //   390: lstore          f1g8
        //   392: iload           f1_2
        //   394: i2l            
        //   395: iload           g9_19
        //   397: i2l            
        //   398: lmul           
        //   399: lstore          f1g9_38
        //   401: aload_0         /* this */
        //   402: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   405: iconst_2       
        //   406: iaload         
        //   407: i2l            
        //   408: aload_2         /* g */
        //   409: iconst_0       
        //   410: iaload         
        //   411: i2l            
        //   412: lmul           
        //   413: lstore          f2g0
        //   415: aload_0         /* this */
        //   416: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   419: iconst_2       
        //   420: iaload         
        //   421: i2l            
        //   422: aload_2         /* g */
        //   423: iconst_1       
        //   424: iaload         
        //   425: i2l            
        //   426: lmul           
        //   427: lstore          f2g1
        //   429: aload_0         /* this */
        //   430: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   433: iconst_2       
        //   434: iaload         
        //   435: i2l            
        //   436: aload_2         /* g */
        //   437: iconst_2       
        //   438: iaload         
        //   439: i2l            
        //   440: lmul           
        //   441: lstore          f2g2
        //   443: aload_0         /* this */
        //   444: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   447: iconst_2       
        //   448: iaload         
        //   449: i2l            
        //   450: aload_2         /* g */
        //   451: iconst_3       
        //   452: iaload         
        //   453: i2l            
        //   454: lmul           
        //   455: lstore          f2g3
        //   457: aload_0         /* this */
        //   458: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   461: iconst_2       
        //   462: iaload         
        //   463: i2l            
        //   464: aload_2         /* g */
        //   465: iconst_4       
        //   466: iaload         
        //   467: i2l            
        //   468: lmul           
        //   469: lstore          f2g4
        //   471: aload_0         /* this */
        //   472: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   475: iconst_2       
        //   476: iaload         
        //   477: i2l            
        //   478: aload_2         /* g */
        //   479: iconst_5       
        //   480: iaload         
        //   481: i2l            
        //   482: lmul           
        //   483: lstore          f2g5
        //   485: aload_0         /* this */
        //   486: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   489: iconst_2       
        //   490: iaload         
        //   491: i2l            
        //   492: aload_2         /* g */
        //   493: bipush          6
        //   495: iaload         
        //   496: i2l            
        //   497: lmul           
        //   498: lstore          f2g6
        //   500: aload_0         /* this */
        //   501: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   504: iconst_2       
        //   505: iaload         
        //   506: i2l            
        //   507: aload_2         /* g */
        //   508: bipush          7
        //   510: iaload         
        //   511: i2l            
        //   512: lmul           
        //   513: lstore          f2g7
        //   515: aload_0         /* this */
        //   516: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   519: iconst_2       
        //   520: iaload         
        //   521: i2l            
        //   522: iload           g8_19
        //   524: i2l            
        //   525: lmul           
        //   526: lstore          f2g8_19
        //   528: aload_0         /* this */
        //   529: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   532: iconst_2       
        //   533: iaload         
        //   534: i2l            
        //   535: iload           g9_19
        //   537: i2l            
        //   538: lmul           
        //   539: lstore          f2g9_19
        //   541: aload_0         /* this */
        //   542: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   545: iconst_3       
        //   546: iaload         
        //   547: i2l            
        //   548: aload_2         /* g */
        //   549: iconst_0       
        //   550: iaload         
        //   551: i2l            
        //   552: lmul           
        //   553: lstore          f3g0
        //   555: iload           f3_2
        //   557: i2l            
        //   558: aload_2         /* g */
        //   559: iconst_1       
        //   560: iaload         
        //   561: i2l            
        //   562: lmul           
        //   563: lstore          f3g1_2
        //   565: aload_0         /* this */
        //   566: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   569: iconst_3       
        //   570: iaload         
        //   571: i2l            
        //   572: aload_2         /* g */
        //   573: iconst_2       
        //   574: iaload         
        //   575: i2l            
        //   576: lmul           
        //   577: lstore          f3g2
        //   579: iload           f3_2
        //   581: i2l            
        //   582: aload_2         /* g */
        //   583: iconst_3       
        //   584: iaload         
        //   585: i2l            
        //   586: lmul           
        //   587: lstore          f3g3_2
        //   589: aload_0         /* this */
        //   590: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   593: iconst_3       
        //   594: iaload         
        //   595: i2l            
        //   596: aload_2         /* g */
        //   597: iconst_4       
        //   598: iaload         
        //   599: i2l            
        //   600: lmul           
        //   601: lstore          f3g4
        //   603: iload           f3_2
        //   605: i2l            
        //   606: aload_2         /* g */
        //   607: iconst_5       
        //   608: iaload         
        //   609: i2l            
        //   610: lmul           
        //   611: lstore          f3g5_2
        //   613: aload_0         /* this */
        //   614: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   617: iconst_3       
        //   618: iaload         
        //   619: i2l            
        //   620: aload_2         /* g */
        //   621: bipush          6
        //   623: iaload         
        //   624: i2l            
        //   625: lmul           
        //   626: lstore          f3g6
        //   628: iload           f3_2
        //   630: i2l            
        //   631: iload           g7_19
        //   633: i2l            
        //   634: lmul           
        //   635: lstore          f3g7_38
        //   637: aload_0         /* this */
        //   638: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   641: iconst_3       
        //   642: iaload         
        //   643: i2l            
        //   644: iload           g8_19
        //   646: i2l            
        //   647: lmul           
        //   648: lstore          f3g8_19
        //   650: iload           f3_2
        //   652: i2l            
        //   653: iload           g9_19
        //   655: i2l            
        //   656: lmul           
        //   657: lstore          f3g9_38
        //   659: aload_0         /* this */
        //   660: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   663: iconst_4       
        //   664: iaload         
        //   665: i2l            
        //   666: aload_2         /* g */
        //   667: iconst_0       
        //   668: iaload         
        //   669: i2l            
        //   670: lmul           
        //   671: lstore          f4g0
        //   673: aload_0         /* this */
        //   674: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   677: iconst_4       
        //   678: iaload         
        //   679: i2l            
        //   680: aload_2         /* g */
        //   681: iconst_1       
        //   682: iaload         
        //   683: i2l            
        //   684: lmul           
        //   685: lstore          f4g1
        //   687: aload_0         /* this */
        //   688: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   691: iconst_4       
        //   692: iaload         
        //   693: i2l            
        //   694: aload_2         /* g */
        //   695: iconst_2       
        //   696: iaload         
        //   697: i2l            
        //   698: lmul           
        //   699: lstore          f4g2
        //   701: aload_0         /* this */
        //   702: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   705: iconst_4       
        //   706: iaload         
        //   707: i2l            
        //   708: aload_2         /* g */
        //   709: iconst_3       
        //   710: iaload         
        //   711: i2l            
        //   712: lmul           
        //   713: lstore          f4g3
        //   715: aload_0         /* this */
        //   716: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   719: iconst_4       
        //   720: iaload         
        //   721: i2l            
        //   722: aload_2         /* g */
        //   723: iconst_4       
        //   724: iaload         
        //   725: i2l            
        //   726: lmul           
        //   727: lstore          f4g4
        //   729: aload_0         /* this */
        //   730: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   733: iconst_4       
        //   734: iaload         
        //   735: i2l            
        //   736: aload_2         /* g */
        //   737: iconst_5       
        //   738: iaload         
        //   739: i2l            
        //   740: lmul           
        //   741: lstore          f4g5
        //   743: aload_0         /* this */
        //   744: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   747: iconst_4       
        //   748: iaload         
        //   749: i2l            
        //   750: iload           g6_19
        //   752: i2l            
        //   753: lmul           
        //   754: lstore          f4g6_19
        //   756: aload_0         /* this */
        //   757: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   760: iconst_4       
        //   761: iaload         
        //   762: i2l            
        //   763: iload           g7_19
        //   765: i2l            
        //   766: lmul           
        //   767: lstore          f4g7_19
        //   769: aload_0         /* this */
        //   770: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   773: iconst_4       
        //   774: iaload         
        //   775: i2l            
        //   776: iload           g8_19
        //   778: i2l            
        //   779: lmul           
        //   780: lstore          f4g8_19
        //   782: aload_0         /* this */
        //   783: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   786: iconst_4       
        //   787: iaload         
        //   788: i2l            
        //   789: iload           g9_19
        //   791: i2l            
        //   792: lmul           
        //   793: lstore          f4g9_19
        //   795: aload_0         /* this */
        //   796: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   799: iconst_5       
        //   800: iaload         
        //   801: i2l            
        //   802: aload_2         /* g */
        //   803: iconst_0       
        //   804: iaload         
        //   805: i2l            
        //   806: lmul           
        //   807: lstore          f5g0
        //   809: iload           f5_2
        //   811: i2l            
        //   812: aload_2         /* g */
        //   813: iconst_1       
        //   814: iaload         
        //   815: i2l            
        //   816: lmul           
        //   817: lstore          f5g1_2
        //   819: aload_0         /* this */
        //   820: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   823: iconst_5       
        //   824: iaload         
        //   825: i2l            
        //   826: aload_2         /* g */
        //   827: iconst_2       
        //   828: iaload         
        //   829: i2l            
        //   830: lmul           
        //   831: lstore          f5g2
        //   833: iload           f5_2
        //   835: i2l            
        //   836: aload_2         /* g */
        //   837: iconst_3       
        //   838: iaload         
        //   839: i2l            
        //   840: lmul           
        //   841: lstore          f5g3_2
        //   843: aload_0         /* this */
        //   844: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   847: iconst_5       
        //   848: iaload         
        //   849: i2l            
        //   850: aload_2         /* g */
        //   851: iconst_4       
        //   852: iaload         
        //   853: i2l            
        //   854: lmul           
        //   855: lstore          f5g4
        //   857: iload           f5_2
        //   859: i2l            
        //   860: iload           g5_19
        //   862: i2l            
        //   863: lmul           
        //   864: lstore          f5g5_38
        //   866: aload_0         /* this */
        //   867: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   870: iconst_5       
        //   871: iaload         
        //   872: i2l            
        //   873: iload           g6_19
        //   875: i2l            
        //   876: lmul           
        //   877: lstore          f5g6_19
        //   879: iload           f5_2
        //   881: i2l            
        //   882: iload           g7_19
        //   884: i2l            
        //   885: lmul           
        //   886: lstore          f5g7_38
        //   888: aload_0         /* this */
        //   889: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   892: iconst_5       
        //   893: iaload         
        //   894: i2l            
        //   895: iload           g8_19
        //   897: i2l            
        //   898: lmul           
        //   899: lstore          f5g8_19
        //   901: iload           f5_2
        //   903: i2l            
        //   904: iload           g9_19
        //   906: i2l            
        //   907: lmul           
        //   908: lstore          f5g9_38
        //   910: aload_0         /* this */
        //   911: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   914: bipush          6
        //   916: iaload         
        //   917: i2l            
        //   918: aload_2         /* g */
        //   919: iconst_0       
        //   920: iaload         
        //   921: i2l            
        //   922: lmul           
        //   923: lstore          f6g0
        //   925: aload_0         /* this */
        //   926: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   929: bipush          6
        //   931: iaload         
        //   932: i2l            
        //   933: aload_2         /* g */
        //   934: iconst_1       
        //   935: iaload         
        //   936: i2l            
        //   937: lmul           
        //   938: lstore          f6g1
        //   940: aload_0         /* this */
        //   941: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   944: bipush          6
        //   946: iaload         
        //   947: i2l            
        //   948: aload_2         /* g */
        //   949: iconst_2       
        //   950: iaload         
        //   951: i2l            
        //   952: lmul           
        //   953: lstore          f6g2
        //   955: aload_0         /* this */
        //   956: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   959: bipush          6
        //   961: iaload         
        //   962: i2l            
        //   963: aload_2         /* g */
        //   964: iconst_3       
        //   965: iaload         
        //   966: i2l            
        //   967: lmul           
        //   968: lstore          f6g3
        //   970: aload_0         /* this */
        //   971: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   974: bipush          6
        //   976: iaload         
        //   977: i2l            
        //   978: iload           g4_19
        //   980: i2l            
        //   981: lmul           
        //   982: lstore          f6g4_19
        //   984: aload_0         /* this */
        //   985: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //   988: bipush          6
        //   990: iaload         
        //   991: i2l            
        //   992: iload           g5_19
        //   994: i2l            
        //   995: lmul           
        //   996: lstore          f6g5_19
        //   998: aload_0         /* this */
        //   999: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1002: bipush          6
        //  1004: iaload         
        //  1005: i2l            
        //  1006: iload           g6_19
        //  1008: i2l            
        //  1009: lmul           
        //  1010: lstore          f6g6_19
        //  1012: aload_0         /* this */
        //  1013: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1016: bipush          6
        //  1018: iaload         
        //  1019: i2l            
        //  1020: iload           g7_19
        //  1022: i2l            
        //  1023: lmul           
        //  1024: lstore          f6g7_19
        //  1026: aload_0         /* this */
        //  1027: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1030: bipush          6
        //  1032: iaload         
        //  1033: i2l            
        //  1034: iload           g8_19
        //  1036: i2l            
        //  1037: lmul           
        //  1038: lstore          f6g8_19
        //  1040: aload_0         /* this */
        //  1041: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1044: bipush          6
        //  1046: iaload         
        //  1047: i2l            
        //  1048: iload           g9_19
        //  1050: i2l            
        //  1051: lmul           
        //  1052: lstore          f6g9_19
        //  1054: aload_0         /* this */
        //  1055: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1058: bipush          7
        //  1060: iaload         
        //  1061: i2l            
        //  1062: aload_2         /* g */
        //  1063: iconst_0       
        //  1064: iaload         
        //  1065: i2l            
        //  1066: lmul           
        //  1067: lstore          f7g0
        //  1069: iload           f7_2
        //  1071: i2l            
        //  1072: aload_2         /* g */
        //  1073: iconst_1       
        //  1074: iaload         
        //  1075: i2l            
        //  1076: lmul           
        //  1077: lstore          f7g1_2
        //  1079: aload_0         /* this */
        //  1080: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1083: bipush          7
        //  1085: iaload         
        //  1086: i2l            
        //  1087: aload_2         /* g */
        //  1088: iconst_2       
        //  1089: iaload         
        //  1090: i2l            
        //  1091: lmul           
        //  1092: lstore          f7g2
        //  1094: iload           f7_2
        //  1096: i2l            
        //  1097: iload           g3_19
        //  1099: i2l            
        //  1100: lmul           
        //  1101: lstore          f7g3_38
        //  1103: aload_0         /* this */
        //  1104: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1107: bipush          7
        //  1109: iaload         
        //  1110: i2l            
        //  1111: iload           g4_19
        //  1113: i2l            
        //  1114: lmul           
        //  1115: lstore          f7g4_19
        //  1117: iload           f7_2
        //  1119: i2l            
        //  1120: iload           g5_19
        //  1122: i2l            
        //  1123: lmul           
        //  1124: lstore          f7g5_38
        //  1126: aload_0         /* this */
        //  1127: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1130: bipush          7
        //  1132: iaload         
        //  1133: i2l            
        //  1134: iload           g6_19
        //  1136: i2l            
        //  1137: lmul           
        //  1138: lstore          f7g6_19
        //  1140: iload           f7_2
        //  1142: i2l            
        //  1143: iload           g7_19
        //  1145: i2l            
        //  1146: lmul           
        //  1147: lstore          f7g7_38
        //  1149: aload_0         /* this */
        //  1150: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1153: bipush          7
        //  1155: iaload         
        //  1156: i2l            
        //  1157: iload           g8_19
        //  1159: i2l            
        //  1160: lmul           
        //  1161: lstore          f7g8_19
        //  1163: iload           f7_2
        //  1165: i2l            
        //  1166: iload           g9_19
        //  1168: i2l            
        //  1169: lmul           
        //  1170: lstore          f7g9_38
        //  1172: aload_0         /* this */
        //  1173: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1176: bipush          8
        //  1178: iaload         
        //  1179: i2l            
        //  1180: aload_2         /* g */
        //  1181: iconst_0       
        //  1182: iaload         
        //  1183: i2l            
        //  1184: lmul           
        //  1185: lstore          f8g0
        //  1187: aload_0         /* this */
        //  1188: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1191: bipush          8
        //  1193: iaload         
        //  1194: i2l            
        //  1195: aload_2         /* g */
        //  1196: iconst_1       
        //  1197: iaload         
        //  1198: i2l            
        //  1199: lmul           
        //  1200: lstore          f8g1
        //  1202: aload_0         /* this */
        //  1203: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1206: bipush          8
        //  1208: iaload         
        //  1209: i2l            
        //  1210: iload           g2_19
        //  1212: i2l            
        //  1213: lmul           
        //  1214: lstore          f8g2_19
        //  1216: aload_0         /* this */
        //  1217: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1220: bipush          8
        //  1222: iaload         
        //  1223: i2l            
        //  1224: iload           g3_19
        //  1226: i2l            
        //  1227: lmul           
        //  1228: lstore          f8g3_19
        //  1230: aload_0         /* this */
        //  1231: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1234: bipush          8
        //  1236: iaload         
        //  1237: i2l            
        //  1238: iload           g4_19
        //  1240: i2l            
        //  1241: lmul           
        //  1242: lstore          f8g4_19
        //  1244: aload_0         /* this */
        //  1245: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1248: bipush          8
        //  1250: iaload         
        //  1251: i2l            
        //  1252: iload           g5_19
        //  1254: i2l            
        //  1255: lmul           
        //  1256: lstore          f8g5_19
        //  1258: aload_0         /* this */
        //  1259: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1262: bipush          8
        //  1264: iaload         
        //  1265: i2l            
        //  1266: iload           g6_19
        //  1268: i2l            
        //  1269: lmul           
        //  1270: lstore          f8g6_19
        //  1272: aload_0         /* this */
        //  1273: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1276: bipush          8
        //  1278: iaload         
        //  1279: i2l            
        //  1280: iload           g7_19
        //  1282: i2l            
        //  1283: lmul           
        //  1284: lstore          f8g7_19
        //  1286: aload_0         /* this */
        //  1287: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1290: bipush          8
        //  1292: iaload         
        //  1293: i2l            
        //  1294: iload           g8_19
        //  1296: i2l            
        //  1297: lmul           
        //  1298: lstore          f8g8_19
        //  1300: aload_0         /* this */
        //  1301: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1304: bipush          8
        //  1306: iaload         
        //  1307: i2l            
        //  1308: iload           g9_19
        //  1310: i2l            
        //  1311: lmul           
        //  1312: lstore          f8g9_19
        //  1314: aload_0         /* this */
        //  1315: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1318: bipush          9
        //  1320: iaload         
        //  1321: i2l            
        //  1322: aload_2         /* g */
        //  1323: iconst_0       
        //  1324: iaload         
        //  1325: i2l            
        //  1326: lmul           
        //  1327: lstore          f9g0
        //  1329: iload           f9_2
        //  1331: i2l            
        //  1332: iload_3         /* g1_19 */
        //  1333: i2l            
        //  1334: lmul           
        //  1335: lstore          f9g1_38
        //  1337: aload_0         /* this */
        //  1338: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1341: bipush          9
        //  1343: iaload         
        //  1344: i2l            
        //  1345: iload           g2_19
        //  1347: i2l            
        //  1348: lmul           
        //  1349: lstore          f9g2_19
        //  1351: iload           f9_2
        //  1353: i2l            
        //  1354: iload           g3_19
        //  1356: i2l            
        //  1357: lmul           
        //  1358: lstore          f9g3_38
        //  1360: aload_0         /* this */
        //  1361: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1364: bipush          9
        //  1366: iaload         
        //  1367: i2l            
        //  1368: iload           g4_19
        //  1370: i2l            
        //  1371: lmul           
        //  1372: lstore          f9g4_19
        //  1374: iload           f9_2
        //  1376: i2l            
        //  1377: iload           g5_19
        //  1379: i2l            
        //  1380: lmul           
        //  1381: lstore          f9g5_38
        //  1383: aload_0         /* this */
        //  1384: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1387: bipush          9
        //  1389: iaload         
        //  1390: i2l            
        //  1391: iload           g6_19
        //  1393: i2l            
        //  1394: lmul           
        //  1395: lstore          f9g6_19
        //  1397: iload           f9_2
        //  1399: i2l            
        //  1400: iload           g7_19
        //  1402: i2l            
        //  1403: lmul           
        //  1404: lstore          f9g7_38
        //  1406: aload_0         /* this */
        //  1407: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.t:[I
        //  1410: bipush          9
        //  1412: iaload         
        //  1413: i2l            
        //  1414: iload           g8_19
        //  1416: i2l            
        //  1417: lmul           
        //  1418: lstore          f9g8_19
        //  1420: iload           f9_2
        //  1422: i2l            
        //  1423: iload           g9_19
        //  1425: i2l            
        //  1426: lmul           
        //  1427: lstore          f9g9_38
        //  1429: lload           f0g0
        //  1431: lload           f1g9_38
        //  1433: ladd           
        //  1434: lload           f2g8_19
        //  1436: ladd           
        //  1437: lload           f3g7_38
        //  1439: ladd           
        //  1440: lload           f4g6_19
        //  1442: ladd           
        //  1443: lload           f5g5_38
        //  1445: ladd           
        //  1446: lload           f6g4_19
        //  1448: ladd           
        //  1449: lload           f7g3_38
        //  1451: ladd           
        //  1452: lload           f8g2_19
        //  1454: ladd           
        //  1455: lload           f9g1_38
        //  1457: ladd           
        //  1458: lstore          h0
        //  1460: lload           f0g1
        //  1462: lload           f1g0
        //  1464: ladd           
        //  1465: lload           f2g9_19
        //  1467: ladd           
        //  1468: lload           f3g8_19
        //  1470: ladd           
        //  1471: lload           f4g7_19
        //  1473: ladd           
        //  1474: lload           f5g6_19
        //  1476: ladd           
        //  1477: lload           f6g5_19
        //  1479: ladd           
        //  1480: lload           f7g4_19
        //  1482: ladd           
        //  1483: lload           f8g3_19
        //  1485: ladd           
        //  1486: lload           f9g2_19
        //  1488: ladd           
        //  1489: lstore          h1
        //  1491: lload           f0g2
        //  1493: lload           f1g1_2
        //  1495: ladd           
        //  1496: lload           f2g0
        //  1498: ladd           
        //  1499: lload           f3g9_38
        //  1501: ladd           
        //  1502: lload           f4g8_19
        //  1504: ladd           
        //  1505: lload           f5g7_38
        //  1507: ladd           
        //  1508: lload           f6g6_19
        //  1510: ladd           
        //  1511: lload           f7g5_38
        //  1513: ladd           
        //  1514: lload           f8g4_19
        //  1516: ladd           
        //  1517: lload           f9g3_38
        //  1519: ladd           
        //  1520: lstore          h2
        //  1522: lload           f0g3
        //  1524: lload           f1g2
        //  1526: ladd           
        //  1527: lload           f2g1
        //  1529: ladd           
        //  1530: lload           f3g0
        //  1532: ladd           
        //  1533: lload           f4g9_19
        //  1535: ladd           
        //  1536: lload           f5g8_19
        //  1538: ladd           
        //  1539: lload           f6g7_19
        //  1541: ladd           
        //  1542: lload           f7g6_19
        //  1544: ladd           
        //  1545: lload           f8g5_19
        //  1547: ladd           
        //  1548: lload           f9g4_19
        //  1550: ladd           
        //  1551: lstore          h3
        //  1553: lload           f0g4
        //  1555: lload           f1g3_2
        //  1557: ladd           
        //  1558: lload           f2g2
        //  1560: ladd           
        //  1561: lload           f3g1_2
        //  1563: ladd           
        //  1564: lload           f4g0
        //  1566: ladd           
        //  1567: lload           f5g9_38
        //  1569: ladd           
        //  1570: lload           f6g8_19
        //  1572: ladd           
        //  1573: lload           f7g7_38
        //  1575: ladd           
        //  1576: lload           f8g6_19
        //  1578: ladd           
        //  1579: lload           f9g5_38
        //  1581: ladd           
        //  1582: lstore          h4
        //  1584: lload           f0g5
        //  1586: lload           f1g4
        //  1588: ladd           
        //  1589: lload           f2g3
        //  1591: ladd           
        //  1592: lload           f3g2
        //  1594: ladd           
        //  1595: lload           f4g1
        //  1597: ladd           
        //  1598: lload           f5g0
        //  1600: ladd           
        //  1601: lload           f6g9_19
        //  1603: ladd           
        //  1604: lload           f7g8_19
        //  1606: ladd           
        //  1607: lload           f8g7_19
        //  1609: ladd           
        //  1610: lload           f9g6_19
        //  1612: ladd           
        //  1613: lstore          h5
        //  1615: lload           f0g6
        //  1617: lload           f1g5_2
        //  1619: ladd           
        //  1620: lload           f2g4
        //  1622: ladd           
        //  1623: lload           f3g3_2
        //  1625: ladd           
        //  1626: lload           f4g2
        //  1628: ladd           
        //  1629: lload           f5g1_2
        //  1631: ladd           
        //  1632: lload           f6g0
        //  1634: ladd           
        //  1635: lload           f7g9_38
        //  1637: ladd           
        //  1638: lload           f8g8_19
        //  1640: ladd           
        //  1641: lload           f9g7_38
        //  1643: ladd           
        //  1644: lstore          h6
        //  1646: lload           f0g7
        //  1648: lload           f1g6
        //  1650: ladd           
        //  1651: lload           f2g5
        //  1653: ladd           
        //  1654: lload           f3g4
        //  1656: ladd           
        //  1657: lload           f4g3
        //  1659: ladd           
        //  1660: lload           f5g2
        //  1662: ladd           
        //  1663: lload           f6g1
        //  1665: ladd           
        //  1666: lload           f7g0
        //  1668: ladd           
        //  1669: lload           f8g9_19
        //  1671: ladd           
        //  1672: lload           f9g8_19
        //  1674: ladd           
        //  1675: lstore          h7
        //  1677: lload           f0g8
        //  1679: lload           f1g7_2
        //  1681: ladd           
        //  1682: lload           f2g6
        //  1684: ladd           
        //  1685: lload           f3g5_2
        //  1687: ladd           
        //  1688: lload           f4g4
        //  1690: ladd           
        //  1691: lload           f5g3_2
        //  1693: ladd           
        //  1694: lload           f6g2
        //  1696: ladd           
        //  1697: lload           f7g1_2
        //  1699: ladd           
        //  1700: lload           f8g0
        //  1702: ladd           
        //  1703: lload           f9g9_38
        //  1705: ladd           
        //  1706: lstore          h8
        //  1708: lload           f0g9
        //  1710: lload           f1g8
        //  1712: ladd           
        //  1713: lload           f2g7
        //  1715: ladd           
        //  1716: lload           f3g6
        //  1718: ladd           
        //  1719: lload           f4g5
        //  1721: ladd           
        //  1722: lload           f5g4
        //  1724: ladd           
        //  1725: lload           f6g3
        //  1727: ladd           
        //  1728: lload           f7g2
        //  1730: ladd           
        //  1731: lload           f8g1
        //  1733: ladd           
        //  1734: lload           f9g0
        //  1736: ladd           
        //  1737: lstore          h9
        //  1739: lload           h0
        //  1741: ldc2_w          33554432
        //  1744: ladd           
        //  1745: bipush          26
        //  1747: lshr           
        //  1748: lstore          carry0
        //  1750: lload           h1
        //  1752: lload           carry0
        //  1754: ladd           
        //  1755: lstore          h1
        //  1757: lload           h0
        //  1759: lload           carry0
        //  1761: bipush          26
        //  1763: lshl           
        //  1764: lsub           
        //  1765: lstore          h0
        //  1767: lload           h4
        //  1769: ldc2_w          33554432
        //  1772: ladd           
        //  1773: bipush          26
        //  1775: lshr           
        //  1776: lstore          carry4
        //  1778: lload           h5
        //  1780: lload           carry4
        //  1782: ladd           
        //  1783: lstore          h5
        //  1785: lload           h4
        //  1787: lload           carry4
        //  1789: bipush          26
        //  1791: lshl           
        //  1792: lsub           
        //  1793: lstore          h4
        //  1795: lload           h1
        //  1797: ldc2_w          16777216
        //  1800: ladd           
        //  1801: bipush          25
        //  1803: lshr           
        //  1804: lstore          carry1
        //  1806: lload           h2
        //  1808: lload           carry1
        //  1810: ladd           
        //  1811: lstore          h2
        //  1813: lload           h1
        //  1815: lload           carry1
        //  1817: bipush          25
        //  1819: lshl           
        //  1820: lsub           
        //  1821: lstore          h1
        //  1823: lload           h5
        //  1825: ldc2_w          16777216
        //  1828: ladd           
        //  1829: bipush          25
        //  1831: lshr           
        //  1832: lstore          carry5
        //  1834: lload           h6
        //  1836: lload           carry5
        //  1838: ladd           
        //  1839: lstore          h6
        //  1841: lload           h5
        //  1843: lload           carry5
        //  1845: bipush          25
        //  1847: lshl           
        //  1848: lsub           
        //  1849: lstore          h5
        //  1851: lload           h2
        //  1853: ldc2_w          33554432
        //  1856: ladd           
        //  1857: bipush          26
        //  1859: lshr           
        //  1860: lstore          carry2
        //  1862: lload           h3
        //  1864: lload           carry2
        //  1866: ladd           
        //  1867: lstore          h3
        //  1869: lload           h2
        //  1871: lload           carry2
        //  1873: bipush          26
        //  1875: lshl           
        //  1876: lsub           
        //  1877: lstore          h2
        //  1879: lload           h6
        //  1881: ldc2_w          33554432
        //  1884: ladd           
        //  1885: bipush          26
        //  1887: lshr           
        //  1888: lstore          carry6
        //  1890: lload           h7
        //  1892: lload           carry6
        //  1894: ladd           
        //  1895: lstore          h7
        //  1897: lload           h6
        //  1899: lload           carry6
        //  1901: bipush          26
        //  1903: lshl           
        //  1904: lsub           
        //  1905: lstore          h6
        //  1907: lload           h3
        //  1909: ldc2_w          16777216
        //  1912: ladd           
        //  1913: bipush          25
        //  1915: lshr           
        //  1916: lstore          carry3
        //  1918: lload           h4
        //  1920: lload           carry3
        //  1922: ladd           
        //  1923: lstore          h4
        //  1925: lload           h3
        //  1927: lload           carry3
        //  1929: bipush          25
        //  1931: lshl           
        //  1932: lsub           
        //  1933: lstore          h3
        //  1935: lload           h7
        //  1937: ldc2_w          16777216
        //  1940: ladd           
        //  1941: bipush          25
        //  1943: lshr           
        //  1944: lstore          carry7
        //  1946: lload           h8
        //  1948: lload           carry7
        //  1950: ladd           
        //  1951: lstore          h8
        //  1953: lload           h7
        //  1955: lload           carry7
        //  1957: bipush          25
        //  1959: lshl           
        //  1960: lsub           
        //  1961: lstore          h7
        //  1963: lload           h4
        //  1965: ldc2_w          33554432
        //  1968: ladd           
        //  1969: bipush          26
        //  1971: lshr           
        //  1972: lstore          carry4
        //  1974: lload           h5
        //  1976: lload           carry4
        //  1978: ladd           
        //  1979: lstore          h5
        //  1981: lload           h4
        //  1983: lload           carry4
        //  1985: bipush          26
        //  1987: lshl           
        //  1988: lsub           
        //  1989: lstore          h4
        //  1991: lload           h8
        //  1993: ldc2_w          33554432
        //  1996: ladd           
        //  1997: bipush          26
        //  1999: lshr           
        //  2000: lstore          carry8
        //  2002: lload           h9
        //  2004: lload           carry8
        //  2006: ladd           
        //  2007: lstore          h9
        //  2009: lload           h8
        //  2011: lload           carry8
        //  2013: bipush          26
        //  2015: lshl           
        //  2016: lsub           
        //  2017: lstore          h8
        //  2019: lload           h9
        //  2021: ldc2_w          16777216
        //  2024: ladd           
        //  2025: bipush          25
        //  2027: lshr           
        //  2028: lstore          carry9
        //  2030: lload           h0
        //  2032: lload           carry9
        //  2034: ldc2_w          19
        //  2037: lmul           
        //  2038: ladd           
        //  2039: lstore          h0
        //  2041: lload           h9
        //  2043: lload           carry9
        //  2045: bipush          25
        //  2047: lshl           
        //  2048: lsub           
        //  2049: lstore          h9
        //  2051: lload           h0
        //  2053: ldc2_w          33554432
        //  2056: ladd           
        //  2057: bipush          26
        //  2059: lshr           
        //  2060: lstore          carry0
        //  2062: lload           h1
        //  2064: lload           carry0
        //  2066: ladd           
        //  2067: lstore          h1
        //  2069: lload           h0
        //  2071: lload           carry0
        //  2073: bipush          26
        //  2075: lshl           
        //  2076: lsub           
        //  2077: lstore          h0
        //  2079: bipush          10
        //  2081: newarray        I
        //  2083: astore_w        257
        //  2087: aload_w         h
        //  2091: iconst_0       
        //  2092: lload           h0
        //  2094: l2i            
        //  2095: iastore        
        //  2096: aload_w         h
        //  2100: iconst_1       
        //  2101: lload           h1
        //  2103: l2i            
        //  2104: iastore        
        //  2105: aload_w         h
        //  2109: iconst_2       
        //  2110: lload           h2
        //  2112: l2i            
        //  2113: iastore        
        //  2114: aload_w         h
        //  2118: iconst_3       
        //  2119: lload           h3
        //  2121: l2i            
        //  2122: iastore        
        //  2123: aload_w         h
        //  2127: iconst_4       
        //  2128: lload           h4
        //  2130: l2i            
        //  2131: iastore        
        //  2132: aload_w         h
        //  2136: iconst_5       
        //  2137: lload           h5
        //  2139: l2i            
        //  2140: iastore        
        //  2141: aload_w         h
        //  2145: bipush          6
        //  2147: lload           h6
        //  2149: l2i            
        //  2150: iastore        
        //  2151: aload_w         h
        //  2155: bipush          7
        //  2157: lload           h7
        //  2159: l2i            
        //  2160: iastore        
        //  2161: aload_w         h
        //  2165: bipush          8
        //  2167: lload           h8
        //  2169: l2i            
        //  2170: iastore        
        //  2171: aload_w         h
        //  2175: bipush          9
        //  2177: lload           h9
        //  2179: l2i            
        //  2180: iastore        
        //  2181: new             Lcom/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement;
        //  2184: dup            
        //  2185: aload_0         /* this */
        //  2186: getfield        com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.f:Lcom/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/Field;
        //  2189: aload_w         h
        //  2193: invokespecial   com/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/ed25519/Ed25519FieldElement.<init>:(Lcom/oceanbase/jdbc/internal/com/send/authentication/ed25519/math/Field;[I)V
        //  2196: areturn        
        // 
        // The error that occurred was:
        // 
        // java.lang.NullPointerException
        //     at com.strobel.decompiler.ast.AstBuilder.convertLocalVariables(AstBuilder.java:2895)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2445)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:211)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:330)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:251)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:126)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    @Override
    public FieldElement square() {
        final int f0 = this.t[0];
        final int f2 = this.t[1];
        final int f3 = this.t[2];
        final int f4 = this.t[3];
        final int f5 = this.t[4];
        final int f6 = this.t[5];
        final int f7 = this.t[6];
        final int f8 = this.t[7];
        final int f9 = this.t[8];
        final int f10 = this.t[9];
        final int f0_2 = 2 * f0;
        final int f1_2 = 2 * f2;
        final int f2_2 = 2 * f3;
        final int f3_2 = 2 * f4;
        final int f4_2 = 2 * f5;
        final int f5_2 = 2 * f6;
        final int f6_2 = 2 * f7;
        final int f7_2 = 2 * f8;
        final int f5_3 = 38 * f6;
        final int f6_3 = 19 * f7;
        final int f7_3 = 38 * f8;
        final int f8_19 = 19 * f9;
        final int f9_38 = 38 * f10;
        final long f0f0 = f0 * (long)f0;
        final long f0f1_2 = f0_2 * (long)f2;
        final long f0f2_2 = f0_2 * (long)f3;
        final long f0f3_2 = f0_2 * (long)f4;
        final long f0f4_2 = f0_2 * (long)f5;
        final long f0f5_2 = f0_2 * (long)f6;
        final long f0f6_2 = f0_2 * (long)f7;
        final long f0f7_2 = f0_2 * (long)f8;
        final long f0f8_2 = f0_2 * (long)f9;
        final long f0f9_2 = f0_2 * (long)f10;
        final long f1f1_2 = f1_2 * (long)f2;
        final long f1f2_2 = f1_2 * (long)f3;
        final long f1f3_4 = f1_2 * (long)f3_2;
        final long f1f4_2 = f1_2 * (long)f5;
        final long f1f5_4 = f1_2 * (long)f5_2;
        final long f1f6_2 = f1_2 * (long)f7;
        final long f1f7_4 = f1_2 * (long)f7_2;
        final long f1f8_2 = f1_2 * (long)f9;
        final long f1f9_76 = f1_2 * (long)f9_38;
        final long f2f2 = f3 * (long)f3;
        final long f2f3_2 = f2_2 * (long)f4;
        final long f2f4_2 = f2_2 * (long)f5;
        final long f2f5_2 = f2_2 * (long)f6;
        final long f2f6_2 = f2_2 * (long)f7;
        final long f2f7_2 = f2_2 * (long)f8;
        final long f2f8_38 = f2_2 * (long)f8_19;
        final long f2f9_38 = f3 * (long)f9_38;
        final long f3f3_2 = f3_2 * (long)f4;
        final long f3f4_2 = f3_2 * (long)f5;
        final long f3f5_4 = f3_2 * (long)f5_2;
        final long f3f6_2 = f3_2 * (long)f7;
        final long f3f7_76 = f3_2 * (long)f7_3;
        final long f3f8_38 = f3_2 * (long)f8_19;
        final long f3f9_76 = f3_2 * (long)f9_38;
        final long f4f4 = f5 * (long)f5;
        final long f4f5_2 = f4_2 * (long)f6;
        final long f4f6_38 = f4_2 * (long)f6_3;
        final long f4f7_38 = f5 * (long)f7_3;
        final long f4f8_38 = f4_2 * (long)f8_19;
        final long f4f9_38 = f5 * (long)f9_38;
        final long f5f5_38 = f6 * (long)f5_3;
        final long f5f6_38 = f5_2 * (long)f6_3;
        final long f5f7_76 = f5_2 * (long)f7_3;
        final long f5f8_38 = f5_2 * (long)f8_19;
        final long f5f9_76 = f5_2 * (long)f9_38;
        final long f6f6_19 = f7 * (long)f6_3;
        final long f6f7_38 = f7 * (long)f7_3;
        final long f6f8_38 = f6_2 * (long)f8_19;
        final long f6f9_38 = f7 * (long)f9_38;
        final long f7f7_38 = f8 * (long)f7_3;
        final long f7f8_38 = f7_2 * (long)f8_19;
        final long f7f9_76 = f7_2 * (long)f9_38;
        final long f8f8_19 = f9 * (long)f8_19;
        final long f8f9_38 = f9 * (long)f9_38;
        final long f9f9_38 = f10 * (long)f9_38;
        long h0 = f0f0 + f1f9_76 + f2f8_38 + f3f7_76 + f4f6_38 + f5f5_38;
        long h2 = f0f1_2 + f2f9_38 + f3f8_38 + f4f7_38 + f5f6_38;
        long h3 = f0f2_2 + f1f1_2 + f3f9_76 + f4f8_38 + f5f7_76 + f6f6_19;
        long h4 = f0f3_2 + f1f2_2 + f4f9_38 + f5f8_38 + f6f7_38;
        long h5 = f0f4_2 + f1f3_4 + f2f2 + f5f9_76 + f6f8_38 + f7f7_38;
        long h6 = f0f5_2 + f1f4_2 + f2f3_2 + f6f9_38 + f7f8_38;
        long h7 = f0f6_2 + f1f5_4 + f2f4_2 + f3f3_2 + f7f9_76 + f8f8_19;
        long h8 = f0f7_2 + f1f6_2 + f2f5_2 + f3f4_2 + f8f9_38;
        long h9 = f0f8_2 + f1f7_4 + f2f6_2 + f3f5_4 + f4f4 + f9f9_38;
        long h10 = f0f9_2 + f1f8_2 + f2f7_2 + f3f6_2 + f4f5_2;
        long carry0 = h0 + 33554432L >> 26;
        h2 += carry0;
        h0 -= carry0 << 26;
        long carry2 = h5 + 33554432L >> 26;
        h6 += carry2;
        h5 -= carry2 << 26;
        final long carry3 = h2 + 16777216L >> 25;
        h3 += carry3;
        h2 -= carry3 << 25;
        final long carry4 = h6 + 16777216L >> 25;
        h7 += carry4;
        h6 -= carry4 << 25;
        final long carry5 = h3 + 33554432L >> 26;
        h4 += carry5;
        h3 -= carry5 << 26;
        final long carry6 = h7 + 33554432L >> 26;
        h8 += carry6;
        h7 -= carry6 << 26;
        final long carry7 = h4 + 16777216L >> 25;
        h5 += carry7;
        h4 -= carry7 << 25;
        final long carry8 = h8 + 16777216L >> 25;
        h9 += carry8;
        h8 -= carry8 << 25;
        carry2 = h5 + 33554432L >> 26;
        h6 += carry2;
        h5 -= carry2 << 26;
        final long carry9 = h9 + 33554432L >> 26;
        h10 += carry9;
        h9 -= carry9 << 26;
        final long carry10 = h10 + 16777216L >> 25;
        h0 += carry10 * 19L;
        h10 -= carry10 << 25;
        carry0 = h0 + 33554432L >> 26;
        h2 += carry0;
        h0 -= carry0 << 26;
        final int[] h11 = { (int)h0, (int)h2, (int)h3, (int)h4, (int)h5, (int)h6, (int)h7, (int)h8, (int)h9, (int)h10 };
        return new Ed25519FieldElement(this.f, h11);
    }
    
    @Override
    public FieldElement squareAndDouble() {
        final int f0 = this.t[0];
        final int f2 = this.t[1];
        final int f3 = this.t[2];
        final int f4 = this.t[3];
        final int f5 = this.t[4];
        final int f6 = this.t[5];
        final int f7 = this.t[6];
        final int f8 = this.t[7];
        final int f9 = this.t[8];
        final int f10 = this.t[9];
        final int f0_2 = 2 * f0;
        final int f1_2 = 2 * f2;
        final int f2_2 = 2 * f3;
        final int f3_2 = 2 * f4;
        final int f4_2 = 2 * f5;
        final int f5_2 = 2 * f6;
        final int f6_2 = 2 * f7;
        final int f7_2 = 2 * f8;
        final int f5_3 = 38 * f6;
        final int f6_3 = 19 * f7;
        final int f7_3 = 38 * f8;
        final int f8_19 = 19 * f9;
        final int f9_38 = 38 * f10;
        final long f0f0 = f0 * (long)f0;
        final long f0f1_2 = f0_2 * (long)f2;
        final long f0f2_2 = f0_2 * (long)f3;
        final long f0f3_2 = f0_2 * (long)f4;
        final long f0f4_2 = f0_2 * (long)f5;
        final long f0f5_2 = f0_2 * (long)f6;
        final long f0f6_2 = f0_2 * (long)f7;
        final long f0f7_2 = f0_2 * (long)f8;
        final long f0f8_2 = f0_2 * (long)f9;
        final long f0f9_2 = f0_2 * (long)f10;
        final long f1f1_2 = f1_2 * (long)f2;
        final long f1f2_2 = f1_2 * (long)f3;
        final long f1f3_4 = f1_2 * (long)f3_2;
        final long f1f4_2 = f1_2 * (long)f5;
        final long f1f5_4 = f1_2 * (long)f5_2;
        final long f1f6_2 = f1_2 * (long)f7;
        final long f1f7_4 = f1_2 * (long)f7_2;
        final long f1f8_2 = f1_2 * (long)f9;
        final long f1f9_76 = f1_2 * (long)f9_38;
        final long f2f2 = f3 * (long)f3;
        final long f2f3_2 = f2_2 * (long)f4;
        final long f2f4_2 = f2_2 * (long)f5;
        final long f2f5_2 = f2_2 * (long)f6;
        final long f2f6_2 = f2_2 * (long)f7;
        final long f2f7_2 = f2_2 * (long)f8;
        final long f2f8_38 = f2_2 * (long)f8_19;
        final long f2f9_38 = f3 * (long)f9_38;
        final long f3f3_2 = f3_2 * (long)f4;
        final long f3f4_2 = f3_2 * (long)f5;
        final long f3f5_4 = f3_2 * (long)f5_2;
        final long f3f6_2 = f3_2 * (long)f7;
        final long f3f7_76 = f3_2 * (long)f7_3;
        final long f3f8_38 = f3_2 * (long)f8_19;
        final long f3f9_76 = f3_2 * (long)f9_38;
        final long f4f4 = f5 * (long)f5;
        final long f4f5_2 = f4_2 * (long)f6;
        final long f4f6_38 = f4_2 * (long)f6_3;
        final long f4f7_38 = f5 * (long)f7_3;
        final long f4f8_38 = f4_2 * (long)f8_19;
        final long f4f9_38 = f5 * (long)f9_38;
        final long f5f5_38 = f6 * (long)f5_3;
        final long f5f6_38 = f5_2 * (long)f6_3;
        final long f5f7_76 = f5_2 * (long)f7_3;
        final long f5f8_38 = f5_2 * (long)f8_19;
        final long f5f9_76 = f5_2 * (long)f9_38;
        final long f6f6_19 = f7 * (long)f6_3;
        final long f6f7_38 = f7 * (long)f7_3;
        final long f6f8_38 = f6_2 * (long)f8_19;
        final long f6f9_38 = f7 * (long)f9_38;
        final long f7f7_38 = f8 * (long)f7_3;
        final long f7f8_38 = f7_2 * (long)f8_19;
        final long f7f9_76 = f7_2 * (long)f9_38;
        final long f8f8_19 = f9 * (long)f8_19;
        final long f8f9_38 = f9 * (long)f9_38;
        final long f9f9_38 = f10 * (long)f9_38;
        long h0 = f0f0 + f1f9_76 + f2f8_38 + f3f7_76 + f4f6_38 + f5f5_38;
        long h2 = f0f1_2 + f2f9_38 + f3f8_38 + f4f7_38 + f5f6_38;
        long h3 = f0f2_2 + f1f1_2 + f3f9_76 + f4f8_38 + f5f7_76 + f6f6_19;
        long h4 = f0f3_2 + f1f2_2 + f4f9_38 + f5f8_38 + f6f7_38;
        long h5 = f0f4_2 + f1f3_4 + f2f2 + f5f9_76 + f6f8_38 + f7f7_38;
        long h6 = f0f5_2 + f1f4_2 + f2f3_2 + f6f9_38 + f7f8_38;
        long h7 = f0f6_2 + f1f5_4 + f2f4_2 + f3f3_2 + f7f9_76 + f8f8_19;
        long h8 = f0f7_2 + f1f6_2 + f2f5_2 + f3f4_2 + f8f9_38;
        long h9 = f0f8_2 + f1f7_4 + f2f6_2 + f3f5_4 + f4f4 + f9f9_38;
        long h10 = f0f9_2 + f1f8_2 + f2f7_2 + f3f6_2 + f4f5_2;
        h0 += h0;
        h2 += h2;
        h3 += h3;
        h4 += h4;
        h5 += h5;
        h6 += h6;
        h7 += h7;
        h8 += h8;
        h9 += h9;
        h10 += h10;
        long carry0 = h0 + 33554432L >> 26;
        h2 += carry0;
        h0 -= carry0 << 26;
        long carry2 = h5 + 33554432L >> 26;
        h6 += carry2;
        h5 -= carry2 << 26;
        final long carry3 = h2 + 16777216L >> 25;
        h3 += carry3;
        h2 -= carry3 << 25;
        final long carry4 = h6 + 16777216L >> 25;
        h7 += carry4;
        h6 -= carry4 << 25;
        final long carry5 = h3 + 33554432L >> 26;
        h4 += carry5;
        h3 -= carry5 << 26;
        final long carry6 = h7 + 33554432L >> 26;
        h8 += carry6;
        h7 -= carry6 << 26;
        final long carry7 = h4 + 16777216L >> 25;
        h5 += carry7;
        h4 -= carry7 << 25;
        final long carry8 = h8 + 16777216L >> 25;
        h9 += carry8;
        h8 -= carry8 << 25;
        carry2 = h5 + 33554432L >> 26;
        h6 += carry2;
        h5 -= carry2 << 26;
        final long carry9 = h9 + 33554432L >> 26;
        h10 += carry9;
        h9 -= carry9 << 26;
        final long carry10 = h10 + 16777216L >> 25;
        h0 += carry10 * 19L;
        h10 -= carry10 << 25;
        carry0 = h0 + 33554432L >> 26;
        h2 += carry0;
        h0 -= carry0 << 26;
        final int[] h11 = { (int)h0, (int)h2, (int)h3, (int)h4, (int)h5, (int)h6, (int)h7, (int)h8, (int)h9, (int)h10 };
        return new Ed25519FieldElement(this.f, h11);
    }
    
    @Override
    public FieldElement invert() {
        FieldElement t0 = this.square();
        FieldElement t2 = t0.square();
        t2 = t2.square();
        t2 = this.multiply(t2);
        t0 = t0.multiply(t2);
        FieldElement t3 = t0.square();
        t2 = t2.multiply(t3);
        t3 = t2.square();
        for (int i = 1; i < 5; ++i) {
            t3 = t3.square();
        }
        t2 = t3.multiply(t2);
        t3 = t2.square();
        for (int i = 1; i < 10; ++i) {
            t3 = t3.square();
        }
        t3 = t3.multiply(t2);
        FieldElement t4 = t3.square();
        for (int i = 1; i < 20; ++i) {
            t4 = t4.square();
        }
        t3 = t4.multiply(t3);
        t3 = t3.square();
        for (int i = 1; i < 10; ++i) {
            t3 = t3.square();
        }
        t2 = t3.multiply(t2);
        t3 = t2.square();
        for (int i = 1; i < 50; ++i) {
            t3 = t3.square();
        }
        t3 = t3.multiply(t2);
        t4 = t3.square();
        for (int i = 1; i < 100; ++i) {
            t4 = t4.square();
        }
        t3 = t4.multiply(t3);
        t3 = t3.square();
        for (int i = 1; i < 50; ++i) {
            t3 = t3.square();
        }
        t2 = t3.multiply(t2);
        t2 = t2.square();
        for (int i = 1; i < 5; ++i) {
            t2 = t2.square();
        }
        return t2.multiply(t0);
    }
    
    @Override
    public FieldElement pow22523() {
        FieldElement t0 = this.square();
        FieldElement t2 = t0.square();
        t2 = t2.square();
        t2 = this.multiply(t2);
        t0 = t0.multiply(t2);
        t0 = t0.square();
        t0 = t2.multiply(t0);
        t2 = t0.square();
        for (int i = 1; i < 5; ++i) {
            t2 = t2.square();
        }
        t0 = t2.multiply(t0);
        t2 = t0.square();
        for (int i = 1; i < 10; ++i) {
            t2 = t2.square();
        }
        t2 = t2.multiply(t0);
        FieldElement t3 = t2.square();
        for (int i = 1; i < 20; ++i) {
            t3 = t3.square();
        }
        t2 = t3.multiply(t2);
        t2 = t2.square();
        for (int i = 1; i < 10; ++i) {
            t2 = t2.square();
        }
        t0 = t2.multiply(t0);
        t2 = t0.square();
        for (int i = 1; i < 50; ++i) {
            t2 = t2.square();
        }
        t2 = t2.multiply(t0);
        t3 = t2.square();
        for (int i = 1; i < 100; ++i) {
            t3 = t3.square();
        }
        t2 = t3.multiply(t2);
        t2 = t2.square();
        for (int i = 1; i < 50; ++i) {
            t2 = t2.square();
        }
        t0 = t2.multiply(t0);
        t0 = t0.square();
        t0 = t0.square();
        return this.multiply(t0);
    }
    
    @Override
    public FieldElement cmov(final FieldElement val, int b) {
        final Ed25519FieldElement that = (Ed25519FieldElement)val;
        b = -b;
        final int[] result = new int[10];
        for (int i = 0; i < 10; ++i) {
            result[i] = this.t[i];
            int x = this.t[i] ^ that.t[i];
            x &= b;
            final int[] array = result;
            final int n = i;
            array[n] ^= x;
        }
        return new Ed25519FieldElement(this.f, result);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.t);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Ed25519FieldElement)) {
            return false;
        }
        final Ed25519FieldElement fe = (Ed25519FieldElement)obj;
        return 1 == Utils.equal(this.toByteArray(), fe.toByteArray());
    }
    
    @Override
    public String toString() {
        return "[Ed25519FieldElement val=" + Utils.bytesToHex(this.toByteArray()) + "]";
    }
    
    static {
        ZERO = new byte[32];
    }
}
