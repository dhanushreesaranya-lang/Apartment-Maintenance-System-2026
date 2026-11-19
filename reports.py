from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt

ROOT=Path(__file__).resolve().parent
DATA=ROOT/"data"
OUT=ROOT/"output"
OUT.mkdir(exist_ok=True)

def main():
    pay=DATA/"payment_report.csv"
    comp=DATA/"complaint_report.csv"
    if not pay.exists() or not comp.exists():
        print("CSV files missing. Run Java menu option 10 first.")
        return
    payments=pd.read_csv(pay)
    complaints=pd.read_csv(comp)
    print("\nPAYMENT REPORT\n",payments.to_string(index=False))
    print("\nCOMPLAINT REPORT\n",complaints.to_string(index=False))
    if not payments.empty:
        payments["payment_status"].value_counts().plot(kind="bar",title="Maintenance Payment Status")
        plt.xlabel("Payment status");plt.ylabel("Number of bills");plt.tight_layout()
        plt.savefig(OUT/"payment_status.png");plt.close()
    if not complaints.empty:
        complaints["status"].value_counts().plot(kind="bar",title="Resident Complaint Status")
        plt.xlabel("Complaint status");plt.ylabel("Number of complaints");plt.tight_layout()
        plt.savefig(OUT/"complaint_status.png");plt.close()
    print("\nCharts saved in",OUT)

if __name__=="__main__":
    main()
