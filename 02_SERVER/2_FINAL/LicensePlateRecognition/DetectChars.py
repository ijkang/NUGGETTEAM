# DetectChars.py

import cv2
import numpy as np
import math
import random

import Main
import Preprocess
import PossibleChar

import pytesseract
import re
import os

# module level variables ##########################################################################
kNearest = cv2.ml.KNearest_create()

# constants for checkIfPossibleChar, this checks one possible char only (does not compare to another char)
MIN_PIXEL_WIDTH = 2
MIN_PIXEL_HEIGHT = 8

#MIN_ASPECT_RATIO = 0.25
MIN_ASPECT_RATIO = 0.2
MAX_ASPECT_RATIO = 1.0

MIN_PIXEL_AREA = 80

# constants for comparing two chars
MIN_DIAG_SIZE_MULTIPLE_AWAY = 0.3
MAX_DIAG_SIZE_MULTIPLE_AWAY = 5.0

MAX_CHANGE_IN_AREA = 0.5

MAX_CHANGE_IN_WIDTH = 0.8
MAX_CHANGE_IN_HEIGHT = 0.2

MAX_ANGLE_BETWEEN_CHARS = 12.0

# other constants
MIN_NUMBER_OF_MATCHING_CHARS = 3

RESIZED_CHAR_IMAGE_WIDTH = 20
RESIZED_CHAR_IMAGE_HEIGHT = 30

MIN_CONTOUR_AREA = 100

###################################################################################################
def loadKNNDataAndTrainKNN():
    allContoursWithData = []                # declare empty lists,
    validContoursWithData = []              # we will fill these shortly

    try:
        # read in training classifications
        if Main.useSever == True:
            npaClassifications = np.loadtxt("./LicensePlateRecognition/classifications.txt", np.float32)
        elif Main.useLocal == True:
            npaClassifications = np.loadtxt("classifications.txt", np.float32)
        # end of if
    except:
        # if file could not be opened, show error message
        print("error, unable to open classifications.txt, exiting program\n")
        os.system("pause")
        return False
    # end try

    try:
        # read in training images
        if Main.useSever == True:
            npaFlattenedImages = np.loadtxt("./LicensePlateRecognition/flattened_images.txt", np.float32)
        elif Main.useLocal == True:
            npaFlattenedImages = np.loadtxt("flattened_images.txt", np.float32)
        # end of if
    except:
        # if file could not be opened, show error message
        print("error, unable to open flattened_images.txt, exiting program\n")
        os.system("pause")
        return False
    # end try

    # reshape numpy array to 1d, necessary to pass to call to train
    npaClassifications = npaClassifications.reshape((npaClassifications.size, 1))

    # set default K to 1
    kNearest.setDefaultK(1)

    # train KNN object
    kNearest.train(npaFlattenedImages, cv2.ml.ROW_SAMPLE, npaClassifications)

    # if we got here training was successful so return true
    return True
# end function

###################################################################################################
def detectCharsInPlates(listOfPossiblePlates):
    intPlateCounter = 0
    imgContours = None
    contours = []

    # if list of possible plates is empty, return
    if len(listOfPossiblePlates) == 0:
        return listOfPossiblePlates
    # end if

    # at this point we can be sure the list of possible plates has at least one plate
    # for each possible plate, this is a big for loop that takes up most of the function
    for possiblePlate in listOfPossiblePlates:
        # preprocess to get grayscale and threshold images
        possiblePlate.imgGrayscale, possiblePlate.imgThresh = Preprocess.preprocess(possiblePlate.imgPlate)

        # increase size of plate image for easier viewing and char detection
        possiblePlate.imgThresh = cv2.resize(possiblePlate.imgThresh, (0, 0), fx = 1.6, fy = 1.6)

        # if Main.useShowImage == True:
        #     cv2.imshow("possiblePlate.imgThresh", possiblePlate.imgThresh)
        #     cv2.waitKey(0)
        # end of if

        # threshold again to eliminate any gray areas
        thresholdValue, possiblePlate.imgThresh = cv2.threshold(possiblePlate.imgThresh, 0.0, 255.0, cv2.THRESH_BINARY | cv2.THRESH_OTSU)

        # find all possible chars in the plate,
        # this function first finds all contours, then only includes contours that could be chars (without comparison to other chars yet)
        listOfPossibleCharsInPlate = findPossibleCharsInPlate(possiblePlate.imgGrayscale, possiblePlate.imgThresh)

        # given a list of all possible chars, find groups of matching chars within the plate
        listOfListsOfMatchingCharsInPlate = findListOfListsOfMatchingChars(listOfPossibleCharsInPlate)

        # if no groups of matching chars were found in the plate
        if (len(listOfListsOfMatchingCharsInPlate) == 0):
            possiblePlate.strChars = ""
            continue # go back to top of for loop
        # end if

        # within each list of matching chars
        for i in range(0, len(listOfListsOfMatchingCharsInPlate)):
            # sort chars from left to right
            listOfListsOfMatchingCharsInPlate[i].sort(key = lambda matchingChar: matchingChar.intCenterX)
            # and remove inner overlapping chars
            listOfListsOfMatchingCharsInPlate[i] = removeInnerOverlappingChars(listOfListsOfMatchingCharsInPlate[i])
        # end for

        # within each possible plate, suppose the longest list of potential matching chars is the actual list of chars
        intLenOfLongestListOfChars = 0
        intIndexOfLongestListOfChars = 0

        # loop through all the vectors of matching chars, get the index of the one with the most chars
        for i in range(0, len(listOfListsOfMatchingCharsInPlate)):
            if len(listOfListsOfMatchingCharsInPlate[i]) > intLenOfLongestListOfChars:
                intLenOfLongestListOfChars = len(listOfListsOfMatchingCharsInPlate[i])
                intIndexOfLongestListOfChars = i
            # end if
        # end for

        # suppose that the longest list of matching chars within the plate is the actual list of chars
        longestListOfMatchingCharsInPlate = listOfListsOfMatchingCharsInPlate[intIndexOfLongestListOfChars]

        possiblePlate.strChars = recognizeCharsInPlate(possiblePlate.imgThresh, longestListOfMatchingCharsInPlate)
    # end of big for loop that takes up most of the function

    return listOfPossiblePlates
# end function

###################################################################################################
def findPossibleCharsInPlate(imgGrayscale, imgThresh):
    listOfPossibleChars = []                        # this will be the return value
    contours = []
    imgThreshCopy = imgThresh.copy()

    # find all contours in plate
    if Main.useSever == True:
        imgContours, contours, npaHierarchy = cv2.findContours(imgThreshCopy, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    elif Main.useLocal == True:
        # find all contours
        contours, _ = cv2.findContours(imgThreshCopy, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    # end of if

    # for each contour
    for contour in contours:
        possibleChar = PossibleChar.PossibleChar(contour)

        # if contour is a possible char, note this does not compare to other chars (yet) . . .
        if checkIfPossibleChar(possibleChar):
            # add to list of possible chars
            listOfPossibleChars.append(possibleChar)
        # end if
    # end if

    return listOfPossibleChars
# end function

###################################################################################################
def checkIfPossibleChar(possibleChar):
    # this function is a 'first pass' that does a rough check on a contour to see if it could be a char,
    # note that we are not (yet) comparing the char to other chars to look for a group
    if (possibleChar.intBoundingRectArea > MIN_PIXEL_AREA and
        possibleChar.intBoundingRectWidth > MIN_PIXEL_WIDTH and possibleChar.intBoundingRectHeight > MIN_PIXEL_HEIGHT and
        MIN_ASPECT_RATIO < possibleChar.fltAspectRatio and possibleChar.fltAspectRatio < MAX_ASPECT_RATIO):
        return True
    else:
        return False
    # end if
# end function

###################################################################################################
def findListOfListsOfMatchingChars(listOfPossibleChars):
    # with this function, we start off with all the possible chars in one big list
    # the purpose of this function is to re-arrange the one big list of chars into a list of lists of matching chars,
    # note that chars that are not found to be in a group of matches do not need to be considered further
    # this will be the return value
    listOfListsOfMatchingChars = []

    # for each possible char in the one big list of chars
    for possibleChar in listOfPossibleChars:
        # find all chars in the big list that match the current char
        listOfMatchingChars = findListOfMatchingChars(possibleChar, listOfPossibleChars)

        # also add the current char to current possible list of matching chars
        listOfMatchingChars.append(possibleChar)

        # #dilector
        # print("possibleChar = " + str(possibleChar))

        # if current possible list of matching chars is not long enough to constitute a possible plate
        if len(listOfMatchingChars) < MIN_NUMBER_OF_MATCHING_CHARS:
            # jump back to the top of the for loop and try again with next char, note that it's not necessary
            # to save the list in any way since it did not have enough chars to be a possible plate
            continue
        # end if

        # if we get here, the current list passed test as a "group" or "cluster" of matching chars
        # so add to our list of lists of matching chars
        listOfListsOfMatchingChars.append(listOfMatchingChars)

        listOfPossibleCharsWithCurrentMatchesRemoved = []

        # remove the current list of matching chars from the big list so we don't use those same chars twice,
        # make sure to make a new big list for this since we don't want to change the original big list
        listOfPossibleCharsWithCurrentMatchesRemoved = list(set(listOfPossibleChars) - set(listOfMatchingChars))

        # recursive call
        recursiveListOfListsOfMatchingChars = findListOfListsOfMatchingChars(listOfPossibleCharsWithCurrentMatchesRemoved)

        # for each list of matching chars found by recursive call
        for recursiveListOfMatchingChars in recursiveListOfListsOfMatchingChars:
            # add to our original list of lists of matching chars
            listOfListsOfMatchingChars.append(recursiveListOfMatchingChars)
        # end for
        break # exit for
    # end for

    return listOfListsOfMatchingChars
# end function

###################################################################################################
def findListOfMatchingChars(possibleChar, listOfChars):
    # the purpose of this function is, given a possible char and a big list of possible chars,
    # find all chars in the big list that are a match for the single possible char, and return those matching chars as a list
    listOfMatchingChars = []                # this will be the return value

    # for each char in big list
    for possibleMatchingChar in listOfChars:
        if possibleMatchingChar == possibleChar:    # if the char we attempting to find matches for is the exact same char as the char in the big list we are currently checking
                                                    # then we should not include it in the list of matches b/c that would end up double including the current char
            continue                                # so do not add to list of matches and jump back to top of for loop
        # end if

        # compute stuff to see if chars are a match
        fltDistanceBetweenChars = distanceBetweenChars(possibleChar, possibleMatchingChar)

        fltAngleBetweenChars = angleBetweenChars(possibleChar, possibleMatchingChar)

        fltChangeInArea = float(abs(possibleMatchingChar.intBoundingRectArea - possibleChar.intBoundingRectArea)) / float(possibleChar.intBoundingRectArea)

        fltChangeInWidth = float(abs(possibleMatchingChar.intBoundingRectWidth - possibleChar.intBoundingRectWidth)) / float(possibleChar.intBoundingRectWidth)
        fltChangeInHeight = float(abs(possibleMatchingChar.intBoundingRectHeight - possibleChar.intBoundingRectHeight)) / float(possibleChar.intBoundingRectHeight)

        # check if chars match
        if (fltDistanceBetweenChars < (possibleChar.fltDiagonalSize * MAX_DIAG_SIZE_MULTIPLE_AWAY) and
            fltAngleBetweenChars < MAX_ANGLE_BETWEEN_CHARS and
            fltChangeInArea < MAX_CHANGE_IN_AREA and
            fltChangeInWidth < MAX_CHANGE_IN_WIDTH and
            fltChangeInHeight < MAX_CHANGE_IN_HEIGHT):

            listOfMatchingChars.append(possibleMatchingChar)        # if the chars are a match, add the current char to list of matching chars
        # end if
    # end for

    return listOfMatchingChars
# end function

###################################################################################################
# use Pythagorean theorem to calculate distance between two chars
def distanceBetweenChars(firstChar, secondChar):
    intX = abs(firstChar.intCenterX - secondChar.intCenterX)
    intY = abs(firstChar.intCenterY - secondChar.intCenterY)

    return math.sqrt((intX ** 2) + (intY ** 2))
# end function

###################################################################################################
# use basic trigonometry (SOH CAH TOA) to calculate angle between chars
def angleBetweenChars(firstChar, secondChar):
    fltAdj = float(abs(firstChar.intCenterX - secondChar.intCenterX))
    fltOpp = float(abs(firstChar.intCenterY - secondChar.intCenterY))

    # check to make sure we do not divide by zero if the center X positions are equal, float division by zero will cause a crash in Python
    if fltAdj != 0.0:
        # if adjacent is not zero, calculate angle
        fltAngleInRad = math.atan(fltOpp / fltAdj)
    else:
        # if adjacent is zero, use this as the angle, this is to be consistent with the C++ version of this program
        fltAngleInRad = 1.5708
    # end if

    # calculate angle in degrees
    fltAngleInDeg = fltAngleInRad * (180.0 / math.pi)

    return fltAngleInDeg
# end function

###################################################################################################
# if we have two chars overlapping or to close to each other to possibly be separate chars, remove the inner (smaller) char,
# this is to prevent including the same char twice if two contours are found for the same char,
# for example for the letter 'O' both the inner ring and the outer ring may be found as contours, but we should only include the char once
def removeInnerOverlappingChars(listOfMatchingChars):
    # this will be the return value
    listOfMatchingCharsWithInnerCharRemoved = list(listOfMatchingChars)

    for currentChar in listOfMatchingChars:
        for otherChar in listOfMatchingChars:
            # if current char and other char are not the same char . . .
            if currentChar != otherChar:
                # if current char and other char have center points at almost the same location . . .
                if distanceBetweenChars(currentChar, otherChar) < (currentChar.fltDiagonalSize * MIN_DIAG_SIZE_MULTIPLE_AWAY):
                                # if we get in here we have found overlapping chars
                                # next we identify which char is smaller, then if that char was not already removed on a previous pass, remove it
                    if currentChar.intBoundingRectArea < otherChar.intBoundingRectArea:         # if current char is smaller than other char
                        if currentChar in listOfMatchingCharsWithInnerCharRemoved:              # if current char was not already removed on a previous pass . . .
                            listOfMatchingCharsWithInnerCharRemoved.remove(currentChar)         # then remove current char
                        # end if
                    else:                                                                       # else if other char is smaller than current char
                        if otherChar in listOfMatchingCharsWithInnerCharRemoved:                # if other char was not already removed on a previous pass . . .
                            listOfMatchingCharsWithInnerCharRemoved.remove(otherChar)           # then remove other char
                        # end if
                    # end if
                # end if
            # end if
        # end for
    # end for

    return listOfMatchingCharsWithInnerCharRemoved
# end function

###################################################################################################
# this is where we apply the actual char recognition
def recognizeCharsInPlate(imgThresh, listOfMatchingChars):
    # this will be the return value, the chars in the lic plate
    strChars = ""

    height, width = imgThresh.shape

    imgThreshColor = np.zeros((height, width, 3), np.uint8)

    # sort chars from left to right
    listOfMatchingChars.sort(key = lambda matchingChar: matchingChar.intCenterX)

    # make color version of threshold image so we can draw contours in color on it
    cv2.cvtColor(imgThresh, cv2.COLOR_GRAY2BGR, imgThreshColor)

    # for each char in plate
    for currentChar in listOfMatchingChars:
        pt1 = (currentChar.intBoundingRectX, currentChar.intBoundingRectY)
        pt2 = ((currentChar.intBoundingRectX + currentChar.intBoundingRectWidth), (currentChar.intBoundingRectY + currentChar.intBoundingRectHeight))

        # draw green box around the char
        cv2.rectangle(imgThreshColor, pt1, pt2, Main.SCALAR_GREEN, 2)

        if Main.useShowImage == True:
            cv2.imshow("imgThreshColor", imgThreshColor)
            cv2.waitKey(0)
        # end of if

        # crop char out of threshold image
        imgROI = imgThresh[currentChar.intBoundingRectY : currentChar.intBoundingRectY + currentChar.intBoundingRectHeight,
                           currentChar.intBoundingRectX : currentChar.intBoundingRectX + currentChar.intBoundingRectWidth]

        # resize image, this is necessary for char recognition
        imgROIResized = cv2.resize(imgROI, (RESIZED_CHAR_IMAGE_WIDTH, RESIZED_CHAR_IMAGE_HEIGHT))

        # if Main.useShowImage == True:
        #     cv2.imshow("imgROIResized", imgROIResized)
        #     cv2.waitKey(0)
        # end of if

        # flatten image into 1d numpy array
        npaROIResized = imgROIResized.reshape((1, RESIZED_CHAR_IMAGE_WIDTH * RESIZED_CHAR_IMAGE_HEIGHT))

        # convert from 1d numpy array of ints to 1d numpy array of floats
        npaROIResized = np.float32(npaROIResized)

        # finally we can call findNearest !!!
        #retval, npaResults, neigh_resp, dists = kNearest.findNearest(npaROIResized, k = 1)
        retval, npaResults, neigh_resp, dists = kNearest.findNearest(npaROIResized, k=1)

        strCurrentChar = str(chr(int(npaResults[0][0])))            # get character from results

        strChars = strChars + strCurrentChar                        # append current char to full string
    # end for

    return strChars
# end function








