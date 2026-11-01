import sys
from PIL import Image
names="AIR CONCRETE HAZARD STEEL GRATING GLASS COAL_HEAP ASPHALT COOLING_SHELL STACK_SHELL PIPE_X PIPE_Y PIPE_Z CONVEYOR_X FENCE WATER HOPPER_N BARREL_COAL BOILER TURBINE_E FLUE SHAFT_X".split()
col={"AIR":(60,90,50),"CONCRETE":(150,150,146),"HAZARD":(230,190,30),"STEEL":(95,105,115),"GRATING":(70,75,80),"GLASS":(120,170,200),
"COAL_HEAP":(30,30,32),"ASPHALT":(55,55,58),"COOLING_SHELL":(190,188,180),"STACK_SHELL":(180,60,50),"PIPE_X":(140,110,70),"CONVEYOR_X":(200,120,40),
"FENCE":(90,90,90),"WATER":(50,100,200),"BARREL_COAL":(80,60,40),"BOILER":(220,80,40),"TURBINE_E":(40,160,170),"FLUE":(230,230,230),"SHAFT_X":(150,150,60)}
for f in sys.argv[1:]:
    L=open(f).read().split("\n"); W,D=map(int,L[0].split())
    S=8; im=Image.new("RGB",(W*S,D*S))
    for z in range(D):
        for x,tok in enumerate(L[1+z].split()):
            o,h=map(int,tok.split(":")); c=col.get(names[o],(200,0,200))
            k=0.75+min(max(h,0),40)/40*0.4; c=tuple(min(255,int(v*k)) for v in c)
            for dy in range(S):
                for dx in range(S): im.putpixel((x*S+dx,z*S+dy),c)
    im.save(f.replace(".txt",".png")); print(f, im.size)
