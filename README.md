# :tomato:Introduction

2017년 기준 전국에 등록되어있는 청각 장애인의 숫자는 30만을 넘어섰고, 매년 그 수가 증가하는 추세입니다. 청각장애인들은 수화라는 의사소통 수단을 가지고 있지만 수화를 알고 있는 상대와의 의사소통만 가능합니다. 이러한 장벽을 해소시키기 위해 지화 번역 시스템을 설계 및 구현하였습니다.


# :tomato:Introduction

장갑이 있어야 실행 가능한 앱입니다.

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

동작 인식을 위해서 손가락의 모양과 손의 방향을 구분해 낼 수 있는 flex 센서와 mpu6050이 사용되었습니다. 사용자 데이터를 수집하여 K-means알고리즘을 통해 군집화하고 손의 모양을 구분합니다. 연산속도 개선을 위해 mpu6050에서 자체적으로 제공하는 자이로 가속도 연산 프로그램이 사용되었습니다. 구현 결과 28개의 지화 중 “ㄹ,ㅍ,ㅖ,ㅠ” 를 제외한 나머지 24개의 지화에서 90% 이상의 높은 인식률을 보였습니다. 
