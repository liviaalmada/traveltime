clear
graphics_toolkit("gnuplot")
load 'gmm01';
mean = gmm01(:,1);
std = sqrt(gmm01(:,2));
w = gmm01(:,3)
x = [0:0.2:1000];
for i = 1:size(mean)
  #if w(i)>0.1

  i
  plot (x,  normpdf(x, mean(i), std(i)))
  hold on
  #endif
endfor
hold off
#print -dpng "-S400,400" normal.png