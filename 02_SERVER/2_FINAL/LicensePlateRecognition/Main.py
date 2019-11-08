# Main.py
import re

import cv2
import numpy as np
import os

import DetectChars
import DetectPlates
import PossiblePlate
import Preprocess

import pytesseract
import sys

# module level variables ##########################################################################
SCALAR_BLACK = (0.0, 0.0, 0.0)
SCALAR_WHITE = (255.0, 255.0, 255.0)
SCALAR_YELLOW = (0.0, 255.0, 255.0)
SCALAR_GREEN = (0.0, 255.0, 0.0)
SCALAR_RED = (0.0, 0.0, 255.0)

# server or local
#useSever = False
useSever = True
#useLocal = True
useLocal = False

# resize or not
#useResize = True
useResize = False

# show image
#useShowImage = True
useShowImage = False

# pytesseract
useTesseract = True
#useTesseract = False

###################################################################################################
def main():
    # attempt KNN training
    blnKNNTrainingSuccessful = DetectChars.loadKNNDataAndTrainKNN()

    # if KNN training was not successful
    if blnKNNTrainingSuccessful == False:
        # show error message and exit program
        print("\nerror: KNN traning was not successful\n")
        return
    # end if

    # image path, open image
    if useSever == True:
        imagefolder = "/var/www/html/newImage/"
        imagename = sys.argv[1]
        imagepath = imagefolder + imagename
        imgOriginalScene = cv2.imread(imagepath)
    elif useLocal == True:
        #imgOriginalScene = cv2.imread("PlateImages/CARPIC_20191024_205218.jpg") # write numbers
        #imgOriginalScene = cv2.imread("PlateImages/CARPIC_20191024_212657.jpg") # picture
        imgOriginalScene = cv2.imread("PlateImages/1.jpg") # internet
        #imgOriginalScene = cv2.imread("PlateImages/2.png")  # internet
    # end of if

    # dilector
    # get image size
    # origin_height, origin_width = imgOriginalScene.shape[:2]
    #
    # # Crop from x, y, w, h -> 100, 200, 300, 400
    # # crop_img = img[200:400, 100:300]
    # crop_img = imgOriginalScene[0:(origin_height-100), 0:(origin_width-10)]
    #
    # # NOTE: its img[y: y + h, x: x + w] and *not* img[x: x + w, y: y + h]
    # cv2.imshow("cropped", crop_img)
    # cv2.waitKey(0)
    # dilector

    if useResize == True:
        imageHeight, imageWidth = imgOriginalScene.shape[:2]
        resizeHeight = int(0.09 * imageHeight)
        resizeWidth = int(0.19 * imageWidth)
        imgOriginalScene = cv2.resize(imgOriginalScene, (resizeHeight, resizeWidth), interpolation = cv2.INTER_CUBIC)
    # end of if

    # if useShowImage == True:
    #     cv2.imshow("imgOriginalScene", imgOriginalScene)
    #     cv2.waitKey(0)
    # end of if

    # if image was not read successfully, print error message to std out
    # pause so user can see error message and exit program
    if imgOriginalScene is None:
        print("\nerror: image not read from file \n\n")
        os.system("pause")
        return
    # end if

    #dilector, image copy for hangul =>
    imgOriginalSceneCopy = imgOriginalScene.copy()
    #dilector, image copy for hangul <=

    # detect plates
    listOfPossiblePlates = DetectPlates.detectPlatesInScene(imgOriginalScene)

    #dilector =>
    for possiblePlate in listOfPossiblePlates:
        imgpossiblePlateCopy = possiblePlate.imgPlate.copy()

        height, width, numChannels = imgpossiblePlateCopy.shape

        imgpossiblePlateCopyimgGrayscale = np.zeros((height, width, 1), np.uint8)
        imgpossiblePlateCopyimgThresh = np.zeros((height, width, 1), np.uint8)
        #imgContours = np.zeros((height, width, 3), np.uint8)

        # preprocess to get grayscale and threshold images
        imgpossiblePlateCopyimgGrayscale, imgpossiblePlateCopyimgThresh = Preprocess.preprocess(imgpossiblePlateCopy)

        # increase size of plate image for easier viewing and char detection
        imgpossiblePlateCopyimgThresh = cv2.resize(imgpossiblePlateCopyimgThresh, (0, 0), fx = 1.6, fy = 1.6)

        # cv2.imshow("imgpossiblePlateCopyimgThresh", imgpossiblePlateCopyimgThresh)
        # cv2.waitKey(0)

        imagetostring = pytesseract.image_to_string(imgpossiblePlateCopyimgThresh, lang="kor", config='--psm 7 --oem 0')
        # print("imgpossiblePlateCopyimgThresh, imagetostring = " + imagetostring)

        charsofkorean = pytesseract.image_to_string(imgpossiblePlateCopyimgThresh, lang="kor")
        hangul = re.compile('[^\u3131-\u3163\uac00-\ud7a3]+')
        result = hangul.sub('', charsofkorean)
        #print("result = " + result)
        break
    #dilector <=

    # detect chars in plates
    listOfPossiblePlates = DetectChars.detectCharsInPlates(listOfPossiblePlates)

    # if no plates were found
    if len(listOfPossiblePlates) == 0:
        if useLocal == True:
            # inform user no plates were found
            print("\nno license plates were detected\n")
        # end of if
    else:
        # if we get in here list of possible plates has at lest one plate

        # sort the list of possible plates in DESCENDING order (most number of chars to least number of chars)
        listOfPossiblePlates.sort(key = lambda possiblePlate: len(possiblePlate.strChars), reverse = True)

        # suppose the plate with the most recognized chars (the first plate in sorted by string length descending order) is the actual plate
        licPlate = listOfPossiblePlates[0]

        # if no chars were found in the plate, show message and exit program
        if len(licPlate.strChars) == 0:
            print("\nno characters were detected\n\n")
            return
        # end if

        if useLocal == True:
            if useShowImage == True:
                # draw red rectangle around plate
                drawRedRectangleAroundPlate(imgOriginalScene, licPlate)
            # end of if
        # end of if

        # write license plate text to std out
        if useLocal == True:
            print("license plate read from image = " + licPlate.strChars)
        # end of if

        # dilector =>
        if result == "":
            # license plate
            print(licPlate.strChars)
        else:
        # license plate includ hangul
        # licPlatewithHangul = licPlate.strChars[0] + licPlate.strChars[1] + result + licPlate.strChars[2] + licPlate.strChars[
        #     3] + licPlate.strChars[4] + licPlate.strChars[5]
            licPlatewithHangul = licPlate.strChars[0] + licPlate.strChars[1] + result + licPlate.strChars[3] + licPlate.strChars[4] + licPlate.strChars[5] + licPlate.strChars[6]
            print(licPlatewithHangul)
        # dilector <=

        if useLocal == True:
            print("----------------------------------------")
        # end of if

        # write license plate text on the image
        if useShowImage == True:
            writeLicensePlateCharsOnImage(imgOriginalScene, licPlate)
        # end of if
    # end if else

    return
# end main

###################################################################################################
def drawRedRectangleAroundPlate(imgOriginalScene, licPlate):
    # get 4 vertices of rotated rect
    p2fRectPoints = cv2.boxPoints(licPlate.rrLocationOfPlateInScene)

    # draw 4 red lines
    cv2.line(imgOriginalScene, tuple(p2fRectPoints[0]), tuple(p2fRectPoints[1]), SCALAR_RED, 2)
    cv2.line(imgOriginalScene, tuple(p2fRectPoints[1]), tuple(p2fRectPoints[2]), SCALAR_RED, 2)
    cv2.line(imgOriginalScene, tuple(p2fRectPoints[2]), tuple(p2fRectPoints[3]), SCALAR_RED, 2)
    cv2.line(imgOriginalScene, tuple(p2fRectPoints[3]), tuple(p2fRectPoints[0]), SCALAR_RED, 2)
# end function

###################################################################################################
def writeLicensePlateCharsOnImage(imgOriginalScene, licPlate):
    # this will be the center of the area the text will be written to
    ptCenterOfTextAreaX = 0
    ptCenterOfTextAreaY = 0

    # this will be the bottom left of the area that the text will be written to
    ptLowerLeftTextOriginX = 0
    ptLowerLeftTextOriginY = 0

    sceneHeight, sceneWidth, sceneNumChannels = imgOriginalScene.shape
    plateHeight, plateWidth, plateNumChannels = licPlate.imgPlate.shape

    # choose a plain jane font
    intFontFace = cv2.FONT_HERSHEY_SIMPLEX
    # base font scale on height of plate area
    fltFontScale = float(plateHeight) / 30.0
    # base font thickness on font scale
    intFontThickness = int(round(fltFontScale * 1.5))

    # call getTextSize
    textSize, baseline = cv2.getTextSize(licPlate.strChars, intFontFace, fltFontScale, intFontThickness)

    # unpack roatated rect into center point, width and height, and angle
    ((intPlateCenterX, intPlateCenterY), (intPlateWidth, intPlateHeight), fltCorrectionAngleInDeg) = licPlate.rrLocationOfPlateInScene

    # make sure center is an integer
    intPlateCenterX = int(intPlateCenterX)
    intPlateCenterY = int(intPlateCenterY)

    # the horizontal location of the text area is the same as the plate
    ptCenterOfTextAreaX = int(intPlateCenterX)

    # if the license plate is in the upper 3/4 of the image
    if intPlateCenterY < (sceneHeight * 0.75):
        # write the chars in below the plate
        ptCenterOfTextAreaY = int(round(intPlateCenterY)) + int(round(plateHeight * 1.6))
    # else if the license plate is in the lower 1/4 of the image
    else:
        # write the chars in above the plate
        ptCenterOfTextAreaY = int(round(intPlateCenterY)) - int(round(plateHeight * 1.6))
    # end if

    # unpack text size width and height
    textSizeWidth, textSizeHeight = textSize

    # calculate the lower left origin of the text area
    ptLowerLeftTextOriginX = int(ptCenterOfTextAreaX - (textSizeWidth / 2))
    # based on the text area center, width, and height
    ptLowerLeftTextOriginY = int(ptCenterOfTextAreaY + (textSizeHeight / 2))

    # write the text on the image
    if useShowImage == True:
        cv2.putText(imgOriginalScene, licPlate.strChars, (ptLowerLeftTextOriginX, ptLowerLeftTextOriginY), intFontFace, fltFontScale, SCALAR_YELLOW, intFontThickness)
    # end of if
# end function

###################################################################################################
if __name__ == "__main__":
    main()


















