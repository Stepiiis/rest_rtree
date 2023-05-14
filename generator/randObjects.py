import sys
import random
from datetime import datetime

def genRand(numRange):
    return random.randint(numRange[0],numRange[1]);

def genArr(numRange, length):
    arr = []
    for _ in range(length):
        arr.append(genRand(numRange))
    return arr

if __name__ == "__main__":
    dimension = 3
    count = 100
    num_range=[0,50]
    for i, arg in enumerate(sys.argv):
        if "-d=" in arg:
            dimension = int(arg.split("=")[1])
        if "-c=" in arg:
            count = int(arg.split("=")[1])
        if "-r=" in arg:
            num_range = [int(x) for x in arg.split("=")[1].split(",")]
    random.seed(datetime.now().timestamp())
    print("//",dimension)
    print("//",count)
    for _ in range(count):
        mbr = [
            genArr(num_range,dimension),
            genArr(num_range,dimension)
        ]
        for i in range(dimension):
            while(mbr[0][i] > mbr[1][i]):
                mbr[1][i] = genRand(num_range)
        print(mbr)
