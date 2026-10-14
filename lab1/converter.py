
def rgb_to_cmyk(r, g, b):
    if (r, g, b) == (0, 0, 0):
        return 0.0, 0.0, 0.0, 100.0
    r_n, g_n, b_n = r/255.0, g/255.0, b/255.0
    k = 1-max(r_n, g_n, b_n)
    c = (1-r_n-k)/(1-k)
    m = (1-g_n-k)/(1-k)
    y = (1-b_n-k)/(1-k)
    return round(c*100, 1), round(m*100, 1), round(y*100, 1), round(k*100, 1)

def cmyk_to_rgb(c, m, y, k):
    c_n, m_n, y_n, k_n = c/100.0, m/100.0, y/100.0, k/100.0
    r=round(255*(1-c_n)*(1-k_n))
    g=round(255*(1-m_n)*(1-k_n))
    b=round(255*(1-y_n)*(1-k_n))
    return max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b))

def rgb_to_hsv(r, g, b, current_h=0):
    r_n, g_n, b_n = r/255.0, g/255.0, b/255.0
    cmax, cmin = max(r_n, g_n, b_n), min(r_n, g_n, b_n)
    diff = cmax-cmin

    if diff == 0:
        h = current_h
    elif cmax == r_n:
        h = (60 * ((g_n - b_n) / diff) + 360) % 360
    elif cmax == g_n:
        h = (60 * ((b_n - r_n) / diff) + 120) % 360
    else:
        h = (60 * ((r_n - g_n) / diff) + 240) % 360

    s = 0 if cmax == 0 else (diff / cmax) * 100
    v = cmax * 100
    return round(h, 1), round(s, 1), round(v, 1)

def hsv_to_rgb(h, s, v):
    s_n, v_n = s/100.0, v/100.0
    c = v_n*s_n
    x = c*(1-abs((h/60.0)%2-1))
    m = v_n-c

    if 0 <= h < 60:
        r_p, g_p, b_p = c, x, 0
    elif 60 <= h < 120:
        r_p, g_p, b_p = x, c, 0
    elif 120 <= h < 180:
        r_p, g_p, b_p = 0, c, x
    elif 180 <= h < 240:
        r_p, g_p, b_p = 0, x, c
    elif 240 <= h < 300:
        r_p, g_p, b_p = x, 0, c
    else:
        r_p, g_p, b_p = c, 0, x

    r=round((r_p+m)*255)
    g=round((g_p+m)*255)
    b=round((b_p+m)*255)
    return max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b))