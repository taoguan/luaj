#!/bin/luaj

-- varlist '=' explist
--[[long comment]]
gvar11, gvar21, gvar22, gvar41, gvar42, gvar43 = nil, false, true, 'test1', "test2", [[long string\n, hello]]
gvar31, gvar32, gvar33, gvar34, gvar35, gvar36, gvar37 = 10, -12, 123456789457, -0XFE, 00010001, 123456789456.12, 1.2345678945613E11
gvar51, gvar52, gvar53 = {3,'test1', false}, {test1=34, ['test2']=55}, {test1=34, ['test2']=55, 4, 7, 9, [2]=8, [100]=53}
print(gvar11,gvar21,gvar22,gvar41,gvar42,gvar43)
print(gvar31,gvar32,gvar33,gvar34,gvar36,gvar36,gvar37)
print(gvar51,gvar52,gvar53);

local var11, var21, var22, var41, var42, var43 = nil, false, true, 'test1', "test2", [[long string\n, hello]]
local var31, var32, var33, var34, var35, var36, var37 = 10, -12, 123456789457, -0XFE, 00010001, 123456789456.12, 1.2345678945613E11
local var51, var52, var53 = {3,'test1', false}, {test1=34, ['test2']=55}, {test1=34, ['test2']=55, 4, 7, 9, [2]=8, [100]=53}
print(var11, var21, var22, var41, var42, var43)
print(var31, var32, var33, var34, var36, var36, var37)
print(var51, var52, var53);

-- function funcname funcbody
function var52.addVar(self,...)
    local arg = {...}
    local len = #arg
    print('arg len = '..len)
    local result = 0;
    for i,v in ipairs(arg) do
      print('i='..i..';v='..v)
      if i == 1 then continue end
      if i == 3 then break end
      result = result + v
    end
    return result
end

function var52:minusVar(...)
    local arg = {...}
    local len = #arg
    print('arg len = '..len)
    local result;
    for i,v in ipairs(arg) do
      print('i='..i..';v='..v)
      if i == 1 then continue end
      if result == nil then result = v end
      result = result - v
      print('result = '..result)
    end
    return result
end

-- functioncall
local testAddVar1 = var52.addVar(var52, 1,2,3,4,5)
local testAddVar2 = var52:addVar(1,2,3,4,6)
print(testAddVar1, testAddVar2)
local testMinusVar1 = var52.minusVar(var52, 1,2,3,4,5)
local testMinusVar2 = var52:minusVar(1,2,3,4,6)
print(testMinusVar1, testMinusVar2)

-- call java method
local javesystem = luajava.bindClass("java.lang.System")
local curTime = javesystem:currentTimeMillis();
print(curTime)
local date = luajava.newInstance("java.util.Date")
print(date.getTime(date))
print(date:getTime())

-- ArithOp test
local var31, var32= 2, -3
print(var31 + var32, var31 - var32, var31 * var32, var32 / var31, var32 // var31, var31 ^ var32, var32 % var31)
print(var31 & var32, var31 | var32, var31 ~ var32, ~var31)
print(var31 << var32, var31 >> var32)
print(var31 > var32, var31 >= var32, var31 < var32, var31 <= var32, var31 == var32, var31 ~= var32)

local var21, var22 = false, true
print(var21 and var22, var21 or var22, not var21)
print(var21, var22)
local fun1 = function()
 local var21, var22 = 7, 8
 local var33 = var31 * var32
 print(var21, var22)
 print(string.format('var33=%s', var33))
end
fun1()
print(var21, var22)

local testContinue1 = 1
while testContinue1 < 10
do
if testContinue1 == 5 then
testContinue1 = testContinue1 + 1;
continue end
if testContinue1 == 7 then break end
print('testContinue1='..testContinue1)
testContinue1 = testContinue1 + 1
end
print('testContinue1-done')

local testContinue2 = 1
repeat
if testContinue2 == 5 then
testContinue2 = testContinue2 + 1;
continue end
if testContinue2 == 7 then break end
print('testContinue2='..testContinue2)
testContinue2 = testContinue2 + 1
until testContinue2 >= 10
print('testContinue2-done')

for testContinue3 = 1,10,1
do
if testContinue3 == 5 then
testContinue3 = testContinue3 + 1;
continue end
if testContinue3 == 7 then break end
print('testContinue3='..testContinue3)
testContinue3 = testContinue3 + 1
end
print('testContinue3-done')

local testContinue4Table = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13}
for i, v in ipairs(testContinue4Table)
do
if i == 5 then
continue end
if i == 7 then break end
print('testContinue4='..i..'-v:'..v)
end
print('testContinue4-done')

return var52
