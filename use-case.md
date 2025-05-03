# 유스케이스 모델

## 유스케이스 다이어그램
<img width="717" alt="image" src="https://github.com/user-attachments/assets/efd1917d-ba1a-4089-a98e-252f0472c257" />

## 유스케이스 텍스트(Casual)
1. 윷놀이 판 디자인: 윷놀이 판을 사각형, 오각형, 육각형으로 선택하여 구성
2. 게임 설정: 참여자 수, 말 개수 선택 구성
3. 말 이동: 윷 결과를 가지고 적용할 말을 선택하여 이동
4. 말 업기: 같은 팀 두 말이 같은 위치에 있을 경우 업기 기능
5. 말 잡기: 다른 팀 끼리 있을 경우 잡고자 하는 팀 말이 잡기 기능
6. 분기점 방향 선택: 분기점에서는 어떤 방향으로 이동할지
7. 윷 던지기: 도/개/걸/윷/모/빽도를 지정 및 랜덤으로 던지기
8. 한 팀이 모든 말을 내보냈을 때 게임 종료

## 유스케이스 텍스트(Fully Dressed)
**UC1: 윷놀이 판 디자인**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 사각형/오각형/육각형 윷놀이 판을 고를 수 있음
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 초기화할 수 있어야 함
- Postconditions: 선택한 윷놀이 판으로 윷놀이 진행
- Main Success Scenario:
  1) 사용자가 게임 시작 화면에 진입한다.
  2) 시스템이 판 형태(사각형, 오각형, 육각형)를 선택할 수 있는 옵션을 제공한다.
  3) 사용자가 원하는 판 형태를 선택한다.
  4) 시스템이 선택된 형태에 따라 노드 및 경로를 초기화한다.
  5) 시스템이 선택된 형태의 윷놀이 판을 시각적으로 출력한다.

**UC2: 게임 설정**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 참여자 수, 말 개수를 고를 수 있음
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 초기화할 수 있어야 함
- Postconditions: 선택한 참여자 수/말 개수 구성으로 윷놀이 진행
- Main Success Scenario: 
  1) 사용자가 게임 설정 화면에 진입한다.
  2) 시스템이 참여자 수와 말 개수 입력창을 표시한다.
  3) 사용자가 참여자 수(2~4명)와 말 개수(2~5개)를 입력한다.
  4) 시스템의 유효성을 검사한다.
  5) 유효할 경우, 설정을 저장하고 게임 준비 상태로 진입한다.
- Extensions: 참여자 수가 2명보다 적거나 4명보다 많을 경우, 말 개수가 2개보다 적거나 5개보다 많을 경우 다시 입력을 요청한다.

**UC3: 말 이동**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 움직일 말을 고를 수 있음
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 이동할 수 있어야 함
- Preconditions: 윷을 던지고, 그 윷의 순서를 정한 상태
- Postconditions: 이동한 위치로 말 이동
- Main Success Scenario:
  1) 시스템이 이동할 수 있는 말을 보여준다.
  2) 사용자가 적용할 말을 클릭하여 선택한다.
  3) 선택된 말이 윷 결과만큼 이동한다.
  4) 시스템은 이동 결과를 반영하고 다음 상태(업기/잡기/분기점 등)를 체크한다.
- Extensions: 두 개 이상의 윷을 던진 경우, 윷 순서를 정한 다음 말 이동 진행

**UC4: 말 업기**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 말을 엎을 수 있음
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 업을 수 있어야 함
- Preconditions: 자신의 말을 자신의 윷을 통해 이동하여 잡을 수 있는 상태
- Postconditions: 자신의 말을 엎어 두 개 이상의 말이 한 번에 움직일 수 있음
- Main Success Scenario:
  1) 시스템은 해당 위치에 같은 팀의 말이 있는지 확인한다.
  2) 같은 팀 말이 있을 경우, 두 말을 겹쳐서 업는다.
- Extensions:

**UC5: 말 잡기**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 말을 잡을 수 있음
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 잡을 수 있어야 함
- Preconditions: 상대 말이 자신의 윷을 통해 이동하여 잡을 수 있는 상태
- Postconditions: 상대 말을 잡고 자기 말을 그 자리에 위치, 말을 잡은 사용자는 윷을 한 번 더 던질 수 있음
- Main Success Scenario:
  1) 시스템은 이동한 위치에 상대 말이 있는지 확인한다.
  2) 상대 팀 말이 존재할 경우, 해당 말을 잡고 말판에서 제거한다.
  3) 잡힌 말은 시작 지점으로 돌아간다.
  4) 잡은 사용자는 윷을 한 번 더 던질 수 있다.
- Extensions:

**UC6: 분기점 방향 선택**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 분기점에 진입해, 방향을 골라야 함
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 방향을 선택할 수 있어야 함
- Preconditions: 분기점으로 말을 이동
- Postconditions: 선택한 방향으로 말을 움직일 수 있음
- Main Success Scenario:
  1) 분기점 위치에 도착한 말에게, 시스템은 이동 가능한 경로들을 제시한다.
  2) 사용자는 원하는 방향(중앙 방향 또는 외곽 방향)을 클릭하여 선택한다.
  3) 시스템은 선택한 경로로 말의 다음 위치를 결정한다.
- Extensions:

**UC7: 윷 던지기**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 윷을 던질 상황
  - System: 사용자의 입력에 따라 게임 내부 상태를 설정하고 오류 없이 윷의 결과가 나와야 함
- Preconditions: 말 이동까지 끝난 상황
- Postconditions: 나온 윷 결과를 토대로 말을 이동
- Main Success Scenario:
  1) 사용자가 <랜덤 윷 던지기> 또는 <지정 윷 던지기> 버튼을 클릭한다.
  2) 시스템은 윷 결과(도/개/걸/윷/모/빽도)를 시각적으로 출력한다.
- Extensions: 윷/모의 결과가 나온 경우 다시 한 번 윷을 던져 진행

**UC8: 한 팀이 모든 말을 내보냈을 때 게임 종료**
- Scope: 윷놀이 게임
- Level: User Goal
- Primary Actor: User
- Stakeholders and Interests
  - User: 모든 말을 다 내보낸 사용자
  - System: 게임 내부 상태를 설정하고 오류 없이 게임을 종료할 수 있어야 함
- Preconditions: 말 이동까지 끝난 상황
- Postconditions: 게임 종료 및 우승자 표시
- Main Success Scenario:
  1) 사용자의 말 이동 결과, 해당 팀의 모든 말이 도착지에 도달한다.
  2) 시스템은 해당 팀의 승리를 감지한다.
  3) 시스템은 승리한 사용자의 정보가 있는 화면을 표시한다.
  4) 시스템은 종료 혹은 재시작 버튼을 만들어 기능을 제공한다.
- Extensions:
