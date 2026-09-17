package com.my.calculator.generated

import com.my.calculator.R

/** SVG viewBox. সব কোঅর্ডিনেট এই স্পেসে। */
const val CALC_VIEWPORT_WIDTH = 2486.6667f
const val CALC_VIEWPORT_HEIGHT = 4912f

data class KeyArea(
    val code: String,          // "key_7"
    val pathData: String,      // SVG path "d" (layer1 transform বেক করা)
    val left: Float, val top: Float, val right: Float, val bottom: Float // bbox
)

/** ⚠️ SVG ডকুমেন্ট-অর্ডারে। হিট-টেস্ট করতে হবে **উল্টো দিক থেকে** (শেষেরটা সবার উপরে)। */
val KEY_AREAS: List<KeyArea> = listOf(
    KeyArea(
        code = "key_shift",
        pathData = "m 181.212946,1831.318159 4.75954,403.4603 166.79498,8.3246 200.21959,0.4105 -4.62855,-243.3156 0.84876,-155.1436 z",
        left = 181.2129f, top = 1831.3182f, right = 552.9871f, bottom = 2243.5136f
    ),
    KeyArea(
        code = "key_mode",
        pathData = "m 1958.113756,2218.419159 -12.3813,-415.8585 -178.6259,-4.545 -143.2236,-4.2504 2.4782,424.1519 153.0019,8.3238 z",
        left = 1623.8830f, top = 1793.7653f, right = 1958.1138f, bottom = 2226.2410f
    ),
    KeyArea(
        code = "key_on",
        pathData = "m 2323.807556,2226.735659 6.1516,-438.8285 -212.5895,-6.2519 -167.1393,-3.3854 8.5486,197.702 14.9024,241.936 z",
        left = 1950.2304f, top = 1778.2699f, right = 2329.9592f, bottom = 2226.7357f
    ),
    KeyArea(
        code = "key_alpha",
        pathData = "m 730.247276,1834.394459 -176.37286,4.7547 10.27344,403.6556 316.22202,1.7409 -0.93566,-198.7627 15.83539,-198.9084 z",
        left = 553.8744f, top = 1834.3945f, right = 895.2696f, bottom = 2244.5457f
    ),
    KeyArea(
        code = "key_integ",
        pathData = "m 876.714406,2262.889359 4.68995,273.9091 -311.75931,-3.6085 -2.86442,-272.969 z",
        left = 566.7806f, top = 2260.2210f, right = 881.4044f, bottom = 2536.7985f
    ),
    KeyArea(
        code = "key_calc",
        pathData = "m 555.710006,2263.107759 -7.59371,270.3602 -333.78471,12.4386 -14.8046,-279.08 z",
        left = 199.5270f, top = 2263.1078f, right = 555.7100f, bottom = 2545.9066f
    ),
    KeyArea(
        code = "key_pow_minus1",
        pathData = "m 1947.124956,2230.511559 -15.3968,308.8185 -309.2168,-1.8534 -0.7518,-298.1125 z",
        left = 1621.7596f, top = 2230.5116f, right = 1947.1250f, bottom = 2539.3301f
    ),
    KeyArea(
        code = "key_logn",
        pathData = "m 2314.972456,2241.395459 -0.8364,287.6915 -361.835,-10.9648 0.446,-290.919 z",
        left = 1952.3011f, top = 2227.2032f, right = 2314.9725f, bottom = 2529.0870f
    ),
    KeyArea(
        code = "key_frac",
        pathData = "m 560.443116,2570.184459 8.32731,248.5727 -357.70168,5.2595 1.37017,-258.3949 z",
        left = 211.0687f, top = 2565.6218f, right = 568.7704f, bottom = 2824.0167f
    ),
    KeyArea(
        code = "key_sqrt",
        pathData = "m 917.663896,2549.711359 4.0277,276.2028 -345.27439,-10.3337 -4.41267,-261.8755 z",
        left = 572.0045f, top = 2549.7114f, right = 921.6916f, bottom = 2825.9142f
    ),
    KeyArea(
        code = "key_pow2",
        pathData = "m 1238.405756,2542.114359 -1.0533,288.6932 -309.0664,-4.9256 -4.5497,-271.5676 z",
        left = 923.7364f, top = 2542.1144f, right = 1238.4058f, bottom = 2830.8076f
    ),
    KeyArea(
        code = "key_pown",
        pathData = "m 1582.374756,2549.809459 4.6891,286.929 -332.2752,-2.675 -6.2318,-284.5136 z",
        left = 1248.5569f, top = 2549.5499f, right = 1587.0639f, bottom = 2836.7385f
    ),
    KeyArea(
        code = "key_log",
        pathData = "m 1936.578856,2548.617759 10.6014,280.0067 -352.4485,1.1646 0.8276,-277.4006 z",
        left = 1594.7318f, top = 2548.6178f, right = 1947.1803f, bottom = 2829.7891f
    ),
    KeyArea(
        code = "key_ln",
        pathData = "m 2299.174056,2546.407559 3.0631,276.6953 -359.1569,7.9767 -2.2988,-280.4986 z",
        left = 1940.7815f, top = 2546.4076f, right = 2302.2372f, bottom = 2831.0796f
    ),
    KeyArea(
        code = "key_neg",
        pathData = "m 574.086856,2825.584559 1.73867,273.2907 -363.48161,-2.5101 2.91552,-257.5927 z",
        left = 212.3439f, top = 2825.5846f, right = 575.8255f, bottom = 3098.8753f
    ),
    KeyArea(
        code = "key_deg",
        pathData = "m 921.580326,2832.487559 -0.27005,267.7498 -343.69505,-0.4678 -0.80215,-275.1026 z",
        left = 576.8131f, top = 2824.6670f, right = 921.5803f, bottom = 3100.2374f
    ),
    KeyArea(
        code = "key_hyp",
        pathData = "m 1256.756756,2844.354659 5.6542,250.3875 -336.41059,4.8334 1.67625,-267.7248 z",
        left = 926.0004f, top = 2831.8508f, right = 1262.4110f, bottom = 3099.5756f
    ),
    KeyArea(
        code = "key_sin",
        pathData = "m 1598.295556,2842.701259 0.4676,243.9187 -332.9787,7.7171 -6.7834,-251.3527 z",
        left = 1259.0011f, top = 2842.7013f, right = 1598.7632f, bottom = 3094.3371f
    ),
    KeyArea(
        code = "key_cos",
        pathData = "m 1940.993456,2836.196459 3.4213,251.9177 -341.5565,-2.8382 -1.0748,-244.6151 z",
        left = 1601.7835f, top = 2836.1965f, right = 1944.4148f, bottom = 3088.1142f
    ),
    KeyArea(
        code = "key_tan",
        pathData = "m 2299.942556,2831.663559 2.9667,259.165 -356.8604,-0.3394 -0.6603,-253.5514 z",
        left = 1945.3886f, top = 2831.6636f, right = 2302.9093f, bottom = 3090.8286f
    ),
    KeyArea(
        code = "key_M_plus",
        pathData = "m 2302.801756,3097.961159 -1.6389,298.9837 -366.4266,1.6029 0.2501,-302.4958 z",
        left = 1934.7363f, top = 3096.0520f, right = 2302.8018f, bottom = 3398.5478f
    ),
    KeyArea(
        code = "key_SD",
        pathData = "m 1934.305656,3091.319459 -2.1769,306.9302 -332.0041,-0.7068 0.561,-304.763 z",
        left = 1600.1247f, top = 3091.3195f, right = 1934.3057f, bottom = 3398.2497f
    ),
    KeyArea(
        code = "key_rparen",
        pathData = "m 1266.127356,3099.622659 332.3441,-8.5532 -4.0451,307.1154 -328.4965,-3.0389 z",
        left = 1265.9299f, top = 3091.0695f, right = 1598.4715f, bottom = 3398.1849f
    ),
    KeyArea(
        code = "key_lparen",
        pathData = "m 1264.046156,3097.545259 -2.0894,295.6909 -342.82506,4.2006 2.1157,-294.484 z",
        left = 919.1317f, top = 3097.5453f, right = 1264.0462f, bottom = 3397.4368f
    ),
    KeyArea(
        code = "key_eng",
        pathData = "m 920.757776,3104.353759 -5.63505,291.6219 -336.36163,-2.131 -1.19231,-290.3232 z",
        left = 577.5688f, top = 3103.5215f, right = 920.7578f, bottom = 3395.9757f
    ),
    KeyArea(
        code = "key_rcl",
        pathData = "m 574.609246,3103.593859 -0.72226,289.7283 -360.97172,4.7541 -1.05304,-298.4343 z",
        left = 211.8622f, top = 3099.6420f, right = 574.6092f, bottom = 3398.0763f
    ),
    KeyArea(
        code = "key_7",
        pathData = "m 657.024286,3402.915059 -0.0266,374.6106 -434.26322,5.4183 -7.34922,-381.4991 z",
        left = 215.3852f, top = 3401.4449f, right = 657.0243f, bottom = 3782.9440f
    ),
    KeyArea(
        code = "key_8",
        pathData = "m 658.924596,3399.187959 401.54856,3.6772 1.2982,376.3206 -402.19448,0.2563 z",
        left = 658.9246f, top = 3399.1880f, right = 1061.7714f, bottom = 3779.4421f
    ),
    KeyArea(
        code = "key_9",
        pathData = "m 1473.222056,3401.847559 5.814,369.2853 -414.1898,2.5648 0.5244,-371.874 z",
        left = 1064.8463f, top = 3401.8237f, right = 1479.0361f, bottom = 3773.6977f
    ),
    KeyArea(
        code = "key_del",
        pathData = "m 1877.033456,3403.096559 3.157,363.3671 -395.8005,4.9346 -7.5253,-367.5045 z",
        left = 1476.8647f, top = 3403.0966f, right = 1880.1905f, bottom = 3771.3983f
    ),
    KeyArea(
        code = "key_ac",
        pathData = "m 1878.802056,3402.421659 435.9807,-0.482 -4.2284,362.3979 -427.7038,0.9632 z",
        left = 1878.8021f, top = 3401.9397f, right = 2314.7828f, bottom = 3765.3008f
    ),
    KeyArea(
        code = "key_div",
        pathData = "m 1884.273056,3769.783459 425.1612,0.6348 -4.5954,327.9898 -420.6268,0.2821 z",
        left = 1884.2121f, top = 3769.7835f, right = 2309.4343f, bottom = 4098.6902f
    ),
    KeyArea(
        code = "key_x",
        pathData = "m 1880.928256,3771.979759 -0.105,327.9398 -385.5411,-1.7289 -13.1534,-320.9778 z",
        left = 1482.1288f, top = 3771.9798f, right = 1880.9283f, bottom = 4099.9196f
    ),
    KeyArea(
        code = "key_6",
        pathData = "m 1481.787856,3778.112359 6.5667,319.5358 -423.4356,-1.8365 0.446,-317.9537 z",
        left = 1064.9190f, top = 3777.8580f, right = 1488.3546f, bottom = 4097.6482f
    ),
    KeyArea(
        code = "key_5",
        pathData = "m 1063.208356,3782.526359 -0.1416,312.8064 -404.72691,1.306 0.91838,-312.9057 z",
        left = 658.3398f, top = 3782.5264f, right = 1063.2084f, bottom = 4096.6388f
    ),
    KeyArea(
        code = "key_4",
        pathData = "m 656.249856,3785.532059 -0.95039,310.3799 -429.25733,9.8707 0.9922,-313.8971 z",
        left = 226.0421f, top = 3785.5321f, right = 656.2499f, bottom = 4105.7827f
    ),
    KeyArea(
        code = "key_1",
        pathData = "m 658.159376,4104.141559 10.41452,306.2919 -427.52747,20.3161 -6.09477,-316.5689 z",
        left = 234.9517f, top = 4104.1416f, right = 668.5739f, bottom = 4430.7496f
    ),
    KeyArea(
        code = "key_2",
        pathData = "m 1069.528556,4099.566559 4.053,324.1727 -402.10373,0.8551 -11.05011,-323.5229 z",
        left = 660.4277f, top = 4099.5666f, right = 1073.5816f, bottom = 4424.5944f
    ),
    KeyArea(
        code = "key_3",
        pathData = "m 1468.580256,4103.609959 6.6951,327.4588 -398.7048,-4.2318 -5.1988,-326.4988 z",
        left = 1071.3718f, top = 4100.3382f, right = 1475.2754f, bottom = 4431.0688f
    ),
    KeyArea(
        code = "key_plus",
        pathData = "m 1879.446256,4104.260759 -0.8377,326.5259 -399.5502,0.3833 -6.3588,-327.4555 z",
        left = 1472.6996f, top = 4103.7145f, right = 1879.4463f, bottom = 4431.1700f
    ),
    KeyArea(
        code = "key_minus",
        pathData = "m 2303.252656,4104.319159 -19.5523,326.2644 -402.12,-1.2113 0.7709,-323.4245 z",
        left = 1881.5804f, top = 4104.3192f, right = 2303.2527f, bottom = 4430.5836f
    ),
    KeyArea(
        code = "key_equals",
        pathData = "m 2287.810856,4435.855259 -77.4335,265.3513 -329.2147,60.7182 0.3807,-326.6896 z",
        left = 1881.1627f, top = 4435.2352f, right = 2287.8109f, bottom = 4761.9248f
    ),
    KeyArea(
        code = "key_Ans",
        pathData = "m 1879.693056,4434.606059 -4.1838,328.3065 -391.3017,3.3334 -3.4024,-328.4391 z",
        left = 1480.8052f, top = 4434.6061f, right = 1879.6931f, bottom = 4766.2460f
    ),
    KeyArea(
        code = "key_pow10",
        pathData = "m 1477.447956,4437.785859 4.5513,330.7991 -409.7239,-1.8138 2.79,-334.1964 z",
        left = 1072.2754f, top = 4432.5748f, right = 1481.9993f, bottom = 4768.5850f
    ),
    KeyArea(
        code = "key_comma",
        pathData = "m 1071.906056,4431.456059 -3.3416,334.8406 -400.99111,0.8508 -4.0612,-337.9474 z",
        left = 663.5121f, top = 4429.2001f, right = 1071.9061f, bottom = 4767.1475f
    ),
    KeyArea(
        code = "key_0",
        pathData = "m 662.165726,4417.833059 0.47223,353.4461 -343.7956,-60.0885 -75.4699,-272.3866 z",
        left = 243.3725f, top = 4417.8331f, right = 662.6380f, bottom = 4771.2792f
    ),
    KeyArea(
        code = "key_dir3",
        pathData = "M 1244.765456,2141.839259 992.76695,1822.5887 c 0,0 333.40825,-113.0508 520.41105,8.9088 17.771,11.5899 -266.8119,310.0219 -266.8119,310.0219 z",
        left = 991.1663f, top = 1775.9949f, right = 1511.5774f, bottom = 2141.8393f
    ),
    KeyArea(
        code = "key_dir1",
        pathData = "m 1242.064456,2144.273159 276.5403,264.6186 c 0,0 -297.225,163.7145 -490.3527,51.7061 -18.353,-10.6442 213.8124,-316.3247 213.8124,-316.3247 z",
        left = 1028.2521f, top = 2144.2732f, right = 1518.6048f, bottom = 2498.8250f
    ),
    KeyArea(
        code = "key_dir2",
        pathData = "m 1240.092456,2139.664659 273.4384,-297.2692 c 0,0 201.0613,228.6064 15.5951,561.3791 -10.3288,18.5324 -289.0335,-264.1099 -289.0335,-264.1099 z",
        left = 1240.0925f, top = 1842.3955f, right = 1607.5480f, bottom = 2403.7746f
    ),
    KeyArea(
        code = "key_dir0",
        pathData = "m 1247.042356,2139.842659 c 0,0 -229.099,327.6265 -235.6454,321.748 -62.98392,-56.5575 -222.27906,-476.4925 -26.0279,-629.1713 16.7456,-13.0277 261.6733,307.4233 261.6733,307.4233 z",
        left = 891.6110f, top = 1832.4194f, right = 1247.0424f, bottom = 2461.5907f
    )
)

data class RectSpec(val left: Float, val top: Float, val width: Float, val height: Float)

val DISPLAY_INPUT  = RectSpec(329.8665f,  886.4254f, 1821.9519f, 590.78076f)
val DISPLAY_OUTPUT = RectSpec(329.8664f, 915.1850f, 1821.9519f, 580f)
val SCROLL_X_BORDER = RectSpec(1948.2517f, 888.9880f, 15.884805f, 188.69521f)
val SCROLL_Y_BORDER = RectSpec(336.4988f, 1442.9185f, 356.84247f, 2.7682812f)

/** key code → keybg drawable res id ম্যাপ */
val KEY_BACKGROUND_DRAWABLES: Map<String, Int> = mapOf(
    "key_shift" to R.drawable.keybg_shift,
    "key_mode" to R.drawable.keybg_mode,
    "key_on" to R.drawable.keybg_on,
    "key_alpha" to R.drawable.keybg_alpha,
    "key_integ" to R.drawable.keybg_integ,
    "key_calc" to R.drawable.keybg_calc,
    "key_pow_minus1" to R.drawable.keybg_pow_minus1,
    "key_logn" to R.drawable.keybg_logn,
    "key_frac" to R.drawable.keybg_frac,
    "key_sqrt" to R.drawable.keybg_sqrt,
    "key_pow2" to R.drawable.keybg_pow2,
    "key_pown" to R.drawable.keybg_pown,
    "key_log" to R.drawable.keybg_log,
    "key_ln" to R.drawable.keybg_ln,
    "key_neg" to R.drawable.keybg_neg,
    "key_deg" to R.drawable.keybg_deg,
    "key_hyp" to R.drawable.keybg_hyp,
    "key_sin" to R.drawable.keybg_sin,
    "key_cos" to R.drawable.keybg_cos,
    "key_tan" to R.drawable.keybg_tan,
    "key_M_plus" to R.drawable.keybg_m_plus,
    "key_SD" to R.drawable.keybg_sd,
    "key_rparen" to R.drawable.keybg_rparen,
    "key_lparen" to R.drawable.keybg_lparen,
    "key_eng" to R.drawable.keybg_eng,
    "key_rcl" to R.drawable.keybg_rcl,
    "key_7" to R.drawable.keybg_7,
    "key_8" to R.drawable.keybg_8,
    "key_9" to R.drawable.keybg_9,
    "key_del" to R.drawable.keybg_del,
    "key_ac" to R.drawable.keybg_ac,
    "key_div" to R.drawable.keybg_div,
    "key_x" to R.drawable.keybg_x,
    "key_6" to R.drawable.keybg_6,
    "key_5" to R.drawable.keybg_5,
    "key_4" to R.drawable.keybg_4,
    "key_1" to R.drawable.keybg_1,
    "key_2" to R.drawable.keybg_2,
    "key_3" to R.drawable.keybg_3,
    "key_plus" to R.drawable.keybg_plus,
    "key_minus" to R.drawable.keybg_minus,
    "key_equals" to R.drawable.keybg_equals,
    "key_Ans" to R.drawable.keybg_ans,
    "key_pow10" to R.drawable.keybg_pow10,
    "key_comma" to R.drawable.keybg_comma,
    "key_0" to R.drawable.keybg_0,
    "key_dir3" to R.drawable.keybg_dir3,
    "key_dir1" to R.drawable.keybg_dir1,
    "key_dir2" to R.drawable.keybg_dir2,
    "key_dir0" to R.drawable.keybg_dir0,
)

/** indicator name → ind drawable res id ম্যাপ */
val INDICATOR_DRAWABLES: Map<String, Int> = mapOf(
    "indicator_shift" to R.drawable.ind_shift,
    "indicator_alpha" to R.drawable.ind_alpha,
    "indicator_sci" to R.drawable.ind_sci,
    "indicator_STO" to R.drawable.ind_sto,
    "indicator_cmplx" to R.drawable.ind_cmplx,
    "indicator_mat" to R.drawable.ind_mat,
    "indicator_fix" to R.drawable.ind_fix,
    "indicator_deg" to R.drawable.ind_deg,
    "indicator_rad" to R.drawable.ind_rad,
    "indicator_gra" to R.drawable.ind_gra,
    "indicator_stat" to R.drawable.ind_stat,
    "indicator_RCL" to R.drawable.ind_rcl,
    "indicator_M" to R.drawable.ind_m,
    "indicator_vct" to R.drawable.ind_vct,
    "indicator_math" to R.drawable.ind_math,
    "indicator_nabla" to R.drawable.ind_nabla,
    "indicator_delta" to R.drawable.ind_delta,
    "indicator_disp" to R.drawable.ind_disp,
)
