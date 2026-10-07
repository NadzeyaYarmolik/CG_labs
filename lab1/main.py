# main.py
import tkinter as tk
import customtkinter as ctk
import converter as conv
from PIL import Image, ImageTk

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

class App(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Lab 1")
        self.geometry("820x560")
        self.r, self.g, self.b = 0, 0, 0
        self.current_h = 0.0
        self.is_updating = False
        self._build_ui()
        self.draw_hue_bar()
        self.update_all_from_rgb(0, 0, 0)

    def _build_ui(self):
        main_frame = ctk.CTkFrame(self)
        main_frame.pack(fill="both", expand=True, padx=10, pady=10)
        left_frame = ctk.CTkFrame(main_frame, fg_color="transparent")
        left_frame.pack(side="left", fill="y", padx=10, pady=10)
        self.sv_canvas = tk.Canvas(left_frame, width=220, height=220, bg="black", highlightthickness=0)
        self.sv_canvas.pack(side="left", padx=(0, 5))
        self.sv_canvas.bind("<Button-1>", self.on_sv_click)
        self.sv_canvas.bind("<B1-Motion>", self.on_sv_click)
        self.hue_canvas = tk.Canvas(left_frame, width=25, height=220, bg="black", highlightthickness=0)
        self.hue_canvas.pack(side="left")
        self.hue_canvas.bind("<Button-1>", self.on_hue_click)
        self.hue_canvas.bind("<B1-Motion>", self.on_hue_click)
        right_frame = ctk.CTkFrame(main_frame, fg_color="transparent")
        right_frame.pack(side="left", fill="both", expand=True, padx=10, pady=10)
        self.preview_box = ctk.CTkFrame(right_frame, height=45, fg_color="#000000", corner_radius=6)
        self.preview_box.pack(fill="x", pady=(0, 10))
        self.entries = {}
        self.sliders = {}
        self._create_group(right_frame, "HSV Модель", [("H", "h", 0, 360), ("S", "s", 0, 100), ("V", "v", 0, 100)])
        self._create_group(right_frame, "RGB Модель", [("R", "r", 0, 255), ("G", "g", 0, 255), ("B", "b", 0, 255)])
        self._create_group(right_frame, "CMYK Модель", [("C", "c", 0, 100), ("M", "m", 0, 100), ("Y", "y", 0, 100), ("K", "k", 0, 100)])
        hex_row = ctk.CTkFrame(right_frame, fg_color="transparent")
        hex_row.pack(fill="x", pady=2)
        ctk.CTkLabel(hex_row, text="HEX: #", width=50, anchor="e").pack(side="left")
        hex_entry = ctk.CTkEntry(hex_row, width=80, height=24)
        hex_entry.pack(side="left")
        hex_entry.bind("<Return>", lambda e: self.on_entry_change("hex"))
        hex_entry.bind("<FocusOut>", lambda e: self.on_entry_change("hex"))
        self.entries["hex"] = hex_entry
        swatches_frame = ctk.CTkFrame(right_frame, fg_color="transparent")
        swatches_frame.pack(fill="x", pady=(10, 0))
        preset_colors = ["#000000", "#FF0000", "#00FF00", "#0000FF", "#00FFFF", "#FF00FF", "#FFFF00", "#808080", "#FFFFFF"]
        for color in preset_colors:
            btn = ctk.CTkButton(
                swatches_frame, text="", width=24, height=24, fg_color=color,
                hover_color=color, corner_radius=3,
                command=lambda c=color: self.on_preset_click(c)
            )
            btn.pack(side="left", padx=2)

    def _create_group(self, parent, title, channels):
        group = ctk.CTkFrame(parent, fg_color="transparent")
        group.pack(fill="x", pady=2)
        ctk.CTkLabel(group, text=title, font=("Arial", 11, "bold")).pack(anchor="w")
        for label_text, key, min_v, max_v in channels:
            row = ctk.CTkFrame(group, fg_color="transparent")
            row.pack(fill="x", pady=1)
            ctk.CTkLabel(row, text=f"{label_text}:", width=20, anchor="e").pack(side="left", padx=(0, 5))
            entry = ctk.CTkEntry(row, width=55, height=22)
            entry.pack(side="left", padx=(0, 5))
            entry.bind("<Return>", lambda e, k=key: self.on_entry_change(k))
            entry.bind("<FocusOut>", lambda e, k=key: self.on_entry_change(k))
            self.entries[key] = entry
            slider = ctk.CTkSlider(
                row, from_=min_v, to=max_v, height=14,
                command=lambda val, k=key: self.on_slider_change(k, val)
            )
            slider.pack(side="left", fill="x", expand=True)
            self.sliders[key] = slider

    def draw_hue_bar(self):
        self.hue_canvas.delete("all")
        for y in range(220):
            hue = (y / 220) * 360
            r, g, b = conv.hsv_to_rgb(hue, 100, 100)
            self.hue_canvas.create_line(0, y, 25, y, fill=f"#{r:02x}{g:02x}{b:02x}")

    def draw_sat_val_square(self):
        self.sv_canvas.delete("all")
        step = 5
        for x in range(0, 220, step):
            for y in range(0, 220, step):
                sat = (x / 220) * 100
                val = (1 - (y / 220)) * 100
                r, g, b = conv.hsv_to_rgb(self.current_h, sat, val)
                self.sv_canvas.create_rectangle(x, y, x + step, y + step, fill=f"#{r:02x}{g:02x}{b:02x}", outline="")

    def on_hue_click(self, event):
        y = max(0, min(220, event.y))
        self.current_h = (y / 220) * 360
        _, s, v = conv.rgb_to_hsv(self.r, self.g, self.b)
        r, g, b = conv.hsv_to_rgb(self.current_h, s, v)
        self.update_all_from_rgb(r, g, b)

    def on_sv_click(self, event):
        x = max(0, min(220, event.x))
        y = max(0, min(220, event.y))
        sat = (x / 220) * 100
        val = (1 - (y / 220)) * 100
        r, g, b = conv.hsv_to_rgb(self.current_h, sat, val)
        self.update_all_from_rgb(r, g, b)

    def on_preset_click(self, hex_code):
        hex_code = hex_code.lstrip('#')
        r, g, b = int(hex_code[0:2], 16), int(hex_code[2:4], 16), int(hex_code[4:6], 16)
        self.update_all_from_rgb(r, g, b)

    def on_slider_change(self, key, value):
        if self.is_updating:
            return
        if key in ["r", "g", "b"]:
            r = int(self.sliders["r"].get())
            g = int(self.sliders["g"].get())
            b = int(self.sliders["b"].get())
        elif key in ["h", "s", "v"]:
            h = float(self.sliders["h"].get())
            s = float(self.sliders["s"].get())
            v = float(self.sliders["v"].get())
            r, g, b = conv.hsv_to_rgb(h, s, v)
        elif key in ["c", "m", "y", "k"]:
            c = float(self.sliders["c"].get())
            m = float(self.sliders["m"].get())
            y = float(self.sliders["y"].get())
            k = float(self.sliders["k"].get())
            r, g, b = conv.cmyk_to_rgb(c, m, y, k)
        else:
            return
        self.update_all_from_rgb(r, g, b)

    def on_entry_change(self, changed_key):
        if self.is_updating:
            return
        try:
            if changed_key in ["r", "g", "b"]:
                r = int(self.entries["r"].get())
                g = int(self.entries["g"].get())
                b = int(self.entries["b"].get())
            elif changed_key in ["h", "s", "v"]:
                h = float(self.entries["h"].get().replace("°", "").replace("%", ""))
                s = float(self.entries["s"].get().replace("%", ""))
                v = float(self.entries["v"].get().replace("%", ""))
                r, g, b = conv.hsv_to_rgb(h, s, v)
            elif changed_key in ["c", "m", "y", "k"]:
                c = float(self.entries["c"].get().replace("%", ""))
                m = float(self.entries["m"].get().replace("%", ""))
                y = float(self.entries["y"].get().replace("%", ""))
                k = float(self.entries["k"].get().replace("%", ""))
                r, g, b = conv.cmyk_to_rgb(c, m, y, k)
            elif changed_key == "hex":
                code = self.entries["hex"].get().lstrip('#')
                r, g, b = int(code[0:2], 16), int(code[2:4], 16), int(code[4:6], 16)
            else:
                return
            self.update_all_from_rgb(r, g, b)
        except ValueError:
            pass

    def update_all_from_rgb(self, r, g, b):
        self.is_updating = True
        self.r, self.g, self.b = r, g, b
        c, m, y, k = conv.rgb_to_cmyk(r, g, b)
        h, s, v = conv.rgb_to_hsv(r, g, b)
        if s > 0:
            self.current_h = h
        self.draw_sat_val_square()
        hex_val = f"{r:02x}{g:02x}{b:02x}".upper()
        self.preview_box.configure(fg_color=f"#{hex_val}")
        values_txt = {
            "r": str(r), "g": str(g), "b": str(b),
            "h": f"{h:.1f}°", "s": f"{s:.1f}%", "v": f"{v:.1f}%",
            "c": f"{c:.1f}%", "m": f"{m:.1f}%", "y": f"{y:.1f}%", "k": f"{k:.1f}%",
            "hex": hex_val
        }
        for key, val in values_txt.items():
            self.entries[key].delete(0, tk.END)
            self.entries[key].insert(0, val)
        values_num = {
            "r": r, "g": g, "b": b,
            "h": h, "s": s, "v": v,
            "c": c, "m": m, "y": y, "k": k
        }
        for key, val in values_num.items():
            if key in self.sliders:
                self.sliders[key].set(val)
        self.is_updating = False

if __name__ == "__main__":
    app = App()
    app.mainloop()