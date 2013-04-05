file_pre = file_pre = "2013-04-05-01-38_TestData_C_1stSecond_10"

file_txt = file_pre . ".txt"
file_out = file_pre . ".png"

# line width
LW = 1

# column positions
pTime   = 21
pAF3    =  5
pF7     =  6
pF3     =  7
pFC5    =  8
pT7     =  9
pP7     = 10
pO1     = 11
pO2     = 12
pP8     = 13
pT8     = 14
pFC6    = 15
pF4     = 16
pF8     = 17
pAF4    = 18
pInd    = 27
pQAF3    =  28
pQF7     =  29
pQF3     =  30
pQFC5    =  31
pQT7     =  32
pQP7     = 33
pQO1     = 34
pQO2     = 35
pQP8     = 36
pQT8     = 37
pQFC6    = 38
pQF4     = 39
pQF8     = 40
pQAF4    = 41


# y axis range (comment out if you want auto scale)
# It is just easier to compare different images with the same scale.
set yrange [3500:5000]

#-----------------------------------------------------------------------------
# Plotting.

set xlabel "time (sec)"
set key below

# Some abbreviations for the plot command:
#   u  : using
#   w  : with
#   l  : lines
#   lw : linewidth
#   t  : title
#   "" : same data file

plot file_txt u (column(pTime)):(column(pAF3)) w l lw LW linecolor rgb "pink" t "AF3" \
   , ""       u (column(pTime)):(column(pF7))  w l lw LW linecolor rgb "blue" t "F7"  \
   , ""       u (column(pTime)):(column(pF3))  w l lw LW linecolor rgb "black" t "F3"  \
   , ""       u (column(pTime)):(column(pFC5)) w l lw LW linecolor rgb "red" t "FC5" \
   , ""       u (column(pTime)):(column(pT7))  w l lw LW linecolor rgb "dark-green" t "T7"  \
   , ""       u (column(pTime)):(column(pP7))  w l lw LW linecolor rgb "orange" t "P7"  \
   , ""       u (column(pTime)):(column(pO1))  w l lw LW linecolor rgb "yellow" t "O1"  \
   , ""       u (column(pTime)):(column(pO2))  w l lw LW linecolor rgb "purple" t "O2"  \
   , ""       u (column(pTime)):(column(pP8))  w l lw LW linecolor rgb "brown" t "P8"  \
   , ""       u (column(pTime)):(column(pT8))  w l lw LW linecolor rgb "grey" t "T8"  \
   , ""       u (column(pTime)):(column(pFC6)) w l lw LW linecolor rgb "turquoise" t "FC6" \
   , ""       u (column(pTime)):(column(pF4))  w l lw LW linecolor rgb "orchid" t "F4"  \
   , ""       u (column(pTime)):(column(pF8))  w l lw LW linecolor rgb "green" t "F8"  \
   , ""       u (column(pTime)):(column(pAF4)) w l lw LW linecolor rgb "dark-blue" t "AF4" \
   , ""       u (column(pTime)):(column(pInd)*3800) w l lw LW lt 0 t "ind" \
   , ""       u (column(pTime)):(column(pQAF3)*50+3500) w l lw LW linecolor rgb "pink" t "QAF3" \
   , ""       u (column(pTime)):(column(pQF7)*50+3500)  w l lw LW linecolor rgb "blue" t "QF7"  \
   , ""       u (column(pTime)):(column(pQF3)*50+3500)  w l lw LW linecolor rgb "black" t "QF3"  \
   , ""       u (column(pTime)):(column(pQFC5)*50+3500) w l lw LW linecolor rgb "red" t "QFC5" \
   , ""       u (column(pTime)):(column(pQT7)*50+3500)  w l lw LW linecolor rgb "dark-green" t "QT7"  \
   , ""       u (column(pTime)):(column(pQP7)*50+3500)  w l lw LW linecolor rgb "orange" t "QP7"  \
   , ""       u (column(pTime)):(column(pQO1)*50+3500)  w l lw LW linecolor rgb "yellow" t "QO1"  \
   , ""       u (column(pTime)):(column(pQO2)*50+3500)  w l lw LW linecolor rgb "purple" t "QO2"  \
   , ""       u (column(pTime)):(column(pQP8)*50+3500)  w l lw LW linecolor rgb "brown" t "QP8"  \
   , ""       u (column(pTime)):(column(pQT8)*50+3500)  w l lw LW linecolor rgb "grey" t "QT8"  \
   , ""       u (column(pTime)):(column(pQFC6)*50+3500) w l lw LW linecolor rgb "turquoise" t "QFC6" \
   , ""       u (column(pTime)):(column(pQF4)*50+3500)  w l lw LW linecolor rgb "orchid" t "QF4"  \
   , ""       u (column(pTime)):(column(pQF8)*50+3500)  w l lw LW linecolor rgb "green" t "QF8"  \
   , ""       u (column(pTime)):(column(pQAF4)*50+3500) w l lw LW linecolor rgb "dark-blue" t "QAF4" 

# waits until you click on the figure (so you have time to see it)
pause mouse

#-----------------------------------------------------------------------------
# Printing into file

# List available terminal types on your computer:
# (run gnuplot and type into its terminal)
# gnuplot> set terminal
# also see: http://www.gnuplotting.org/output-terminals/

# set terminal jpeg
set terminal png

set output file_out
replot

