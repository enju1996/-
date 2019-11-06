# :tomato:Introduction

2017년 기준 전국에 등록되어있는 청각 장애인의 숫자는 30만을 넘어섰고, 매년 그 수가 증가하는 추세입니다. 청각장애인들은 수화라는 의사소통 수단을 가지고 있지만 수화를 알고 있는 상대와의 의사소통만 가능합니다. 이러한 장벽을 해소시키기 위해 지화 번역 시스템을 설계 및 구현하였습니다.




# :tomato: instructions
## 사용자 시나리오
1.지화 번역 장갑과 핸드폰을 준비한다.   
2.스마트폰의 어플을 실행하여 블루투스로 스마트폰과 장갑을 연동한다.  
3.장갑을 착용하고 구부림, 펼침를 동작하여 사용자 데이터를 수집한다.  
4.지화 시작 버튼을 누르고 지화를 시작한다.  

## 앱 시나리오
1.앱은 근처에 있는 디바이스를 검색하고 사용자가 선택한 기기와 블루투스 연결을 한다.  
2.장갑으로부터 수신된 데이터가 안정화 될때까지 대기한다.  
3.사용자로부터 구부림과 펼친 상태를 손가락별로 각각 수집해 일반화한다.  
4.지화 시작 버튼이 눌리면 일반화된 데이터를 기반으로 현재 손의 모양과 방향을 분석한다.  
5.DB에 입력된 데이터와 현재 손의 모양을 비교하여 의미를 확정하고 한글 입력 조건 해당여부를 확인한다.  
6.해당되는 지화를 글자로 조합하여 화면에 출력한다.  




# :tomato:requirement

*장갑이 있어야 실행 가능한 앱입니다.*
아두이노 나노와 핸드폰은 블루투스 통신을 합니다. (앱에 블루투스 켜기 및 기기연결 기능이 있습니다.)
엄지/검지/중지/약지/새끼/방향/데이터 안정화 여부 순서대로 안드로이드 앱으로 데이터를 전송하며, 앱은 그 데이터를 기반으로 일반화 및 비교분석하여 결과를 도출합니다. 따라서 손가락 5개에 센서가 모두 부착되어야 하며 추가로 가속도센서, 블루투스 모듈, 아두이노 나노가 있어야 합니다.

엄지~새끼는 각각의 굽힘 정도를 뜻합니다. 손의 방향에 따라 의미가 바뀌는 지화 특성에 따라 손의 방향을 가속도 센서로 구분해야 하고 가속도 센서의 데이터가 안정화 될 때까지 대기 할 필요가 있습니다. 만약 손가락에 부착된 센서가 50, 10, 12, 9, 8 이고 손등이 천장을 바라보는 방향이라면 아두이노는 앱에게 50/20/12/9/8/3(손등을 나타내는 코드)/0(안정화 되지 않음) 형태의 데이터를 전송해 주어야 합니다.

 MPU6050 IMU 에는 가속도계 및 자이로 스코프 데이터를 통합하여 각 센서 고유의 오류 영향을 최소화하는 DMP가 포함되어 있습니다. MPU6050으로 측정된 각속도 값 및 가속도 값으로 각도를 구하는 방법은 여러 가지가 있으나 지화 번역 장갑에서는 DMP를 사용하였다. DMP는 쿼터니언으로 결과를 계산하고 오일러 각으로 변환한다. 라이브러리는 Jeff Rowberg의 i2cdevlib을 사용하였습니다.

장갑 제작 결과는 아래 사진과 같습니다.

![장갑](https://user-images.githubusercontent.com/28775816/68309209-6d5ab180-00f1-11ea-843c-9965cda7ec8e.png)


센서 이름 | 채택 이유 및 용도
--------- | ---------
아두이노 나노 | 가벼운 무게와 저전력으로 오래 작동할 수 있습니다.
플렉스 센서 | 금속 패드가 굴곡이 생기면 저항 값이 변하는 특성을 갖습니다. 이러한 성질을 이용하여 손가락의 구부림을 측정하기 위해 장착하였습니다.
MPU6050 가속도센서 | 손의 방향을 구분하기 위해 사용되었습니다. 연산속도를 보장하기 위해 자이로 가속도 연산 프로그램을 사용하였습다.
HC-06 블루투스 모듈| 데이터는 HC-06을 통해 모바일 기기의 블루투스 Master 모듈로 전송합니다.



 # Directory structure

java file route : project\app\src\main\java\com\example\user\project

bluetoothService : 블루투스 연결 및 데이터 필터링 

CheckInputDataActivity : 나노로부터 데이터가 들어오는지 확인 및 센서 안정화 여부 확인 

DeviceListActivity : 연결 가능한 블루투스 장치 검색 

DropDataActivicy : 사용자 데이터 삭제

FindSign : 지화 내용을 한글로 바꿔줌

Game : 지화를 익힐 수 있는 게임

GameClearActivity : 게임 클리어 시 최고기록 표시

InputData : 사용자 데이터 수집 

InputDataBlowActivity : 주먹을 쥐었을 때 사용자 데이터 수집 

InputDataSpreedActivity : 펼쳤을 때 사용자 데이터 수집

InputDataSQLiteOpenHandler : DB핸들러

Kmeans : 삽입된 데이터로 Kmeans를 수행 

WriteResultActivity : 변역 결과를 출력



# result

동작 인식을 위해서 손가락의 모양과 손의 방향을 구분해 낼 수 있는 flex 센서와 mpu6050이 사용되었습니다. 사용자 데이터를 수집하여 K-means알고리즘을 통해 군집화하고 손의 모양을 구분합니다. 연산속도 개선을 위해 mpu6050에서 자체적으로 제공하는 자이로 가속도 연산 프로그램이 사용되었습니다. 구현 결과 28개의 지화 중 “ㄹ,ㅍ,ㅖ,ㅠ” 를 제외한 나머지 24개의 지화에서 90% 이상의 높은 인식률을 보였습니다. 
